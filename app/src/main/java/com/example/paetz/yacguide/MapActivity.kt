package com.example.paetz.yacguide

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.example.paetz.yacguide.database.AppDatabase
import com.example.paetz.yacguide.map.RockBitmapManager
import com.example.paetz.yacguide.map.RockGeoItem
import com.example.paetz.yacguide.map.cluster.ClusterManager
import com.example.paetz.yacguide.utils.FileDownloader
import com.example.paetz.yacguide.utils.IntentConstants
import com.example.paetz.yacguide.utils.NetworkUtils
import com.example.paetz.yacguide.utils.unpackZip
import org.mapsforge.core.graphics.*

import org.mapsforge.core.model.LatLong
import org.mapsforge.core.model.MapPosition
import org.mapsforge.map.android.graphics.AndroidGraphicFactory
import org.mapsforge.map.android.layers.MyLocationOverlay
import org.mapsforge.map.android.util.AndroidPreferences
import org.mapsforge.map.android.util.AndroidUtil
import org.mapsforge.map.android.view.MapView
import org.mapsforge.map.datastore.MapDataStore
import org.mapsforge.map.datastore.MultiMapDataStore
import org.mapsforge.map.layer.cache.TileCache
import org.mapsforge.map.layer.overlay.Circle
import org.mapsforge.map.layer.overlay.Marker
import org.mapsforge.map.model.IMapViewPosition
import org.mapsforge.map.model.common.PreferencesFacade
import org.mapsforge.map.reader.MapFile
import org.mapsforge.map.reader.header.MapFileException
import org.mapsforge.map.rendertheme.ExternalRenderTheme
import org.mapsforge.map.rendertheme.InternalRenderTheme
import org.mapsforge.map.rendertheme.XmlRenderTheme
import java.io.*
import java.lang.Double.isNaN
import java.util.ArrayList


open class MapActivity : BaseNavigationActivity(), LocationListener {
    private lateinit var mapView: MapView
    private lateinit var preferencesFacade: PreferencesFacade
    private lateinit var locationManager: LocationManager
    private var myLocationOverlay: MyLocationOverlay? = null
    private var clusterer: ClusterManager<RockGeoItem>? = null
    private val tileCaches: MutableList<TileCache> = ArrayList()
    private val mapFileName: String = "sachsen.map"
    private val worldMapFileName: String = "world.map"

    /**
     * Combines map file directory and map file to a map file.
     * This method usually will not need to be changed.
     */
    private val mapFile: MapDataStore
        get() = MapFile(File(mapFileDirectory, mapFileName))

    /**
     * the low res world map file
     */
    private val worldMapFile: MapDataStore
        get() = MapFile(File(mapFileDirectory, worldMapFileName))

    /**
     * The persistable ID is used to store settings information, like the center of the last view
     * and the zoom level. By default the simple name of the class is used. The value is not user
     * visible.
     */
    private val persistableId: String = this.javaClass.simpleName

    /**
     * Returns the relative size of a map view in relation to the screen size of the device. This
     * is used for cache size calculations.
     * By default this returns 1.0, for a full size map view.
     */
    private val screenRatio: Float = 1.0f

    /**
     * Configuration method to set if map view activity's zoom controls hide automatically.
     */
    private val isZoomControlsAutoHide: Boolean = true

    private val initialPosition: MapPosition
        get() {
            val lat = this.intent.getDoubleExtra("lat", 0.0)
            val lon = this.intent.getDoubleExtra("lon", 0.0)
            return MapPosition(LatLong(lat, lon), 8.toByte())
        }

    /**
     * the minimum zoom level of the map view.
     */
    private val zoomLevelMin: Byte
        get() = mapView.model.mapViewPosition.zoomLevelMin

    /**
     * the maximum zoom level of the map view.
     */
    private val zoomLevelMax: Byte
        get() = mapView.model.mapViewPosition.zoomLevelMax

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        createSharedPreferences()
        createMapViews()
        createTileCaches()
        createLayers()
        createControls()
        enableAvailableProviders()
    }

    override fun onDestroy() {
        this.locationManager.removeUpdates(this)

        clusterer?.destroyGeoClusterer()
        this.mapView.model.frameBufferModel.removeObserver(this.clusterer)
        clusterer = null

        this.myLocationOverlay = null
        mapView.destroyAll()
        AndroidGraphicFactory.clearResourceMemoryCache()
        tileCaches.clear()

        super.onDestroy()
    }

    private fun retrieveDbItems(): List<RockGeoItem> {
        val db = AppDatabase.getAppDatabase(this)

        val itemFilter = intent.getIntArrayExtra(IntentConstants.SUMMIT_FILTER)

        val result: MutableList<RockGeoItem> = ArrayList()
        for (rock in db.rockDao().getAllRocks()) {
            if (itemFilter.isNotEmpty() && itemFilter.all { it != rock.id }) {
                continue
            }

            if (rock.latitude != 0f && rock.longitude != 0f) {
                result.add(RockGeoItem(
                        rock.id,
                        rock.name.orEmpty(),
                        LatLong(rock.latitude.toDouble(),
                                rock.longitude.toDouble())))
            }
        }
        return result
    }


    /**
     * Android Activity life cycle method.
     */
    override fun onPause() {
        mapView.model.save(this.preferencesFacade)
        this.preferencesFacade.save()

        super.onPause()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.map, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.action_download -> download()
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    /**
     * Template method to create the map views.
     */
    private fun createMapViews() {
        mapView = findViewById<View>(mapViewId) as MapView
        mapView.model.init(this.preferencesFacade)
        mapView.isClickable = true
        mapView.mapScaleBar.isVisible = true
        mapView.setBuiltInZoomControls(true)
        mapView.mapZoomControls.isAutoHide = isZoomControlsAutoHide
        mapView.mapZoomControls.zoomLevelMin = zoomLevelMin
        mapView.mapZoomControls.zoomLevelMax = zoomLevelMax
    }

    /**
     * Hook to create controls, such as scale bars.
     * You can add more controls.
     */
    private fun createControls() {
        initializePosition(mapView.model.mapViewPosition)
    }

    /**
     * initializes the map view position.
     *
     * @param mvp the map view position to be set
     * @return the mapviewposition set
     */
    private fun initializePosition(mvp: IMapViewPosition): IMapViewPosition {
        val center = mvp.center

        if (center == LatLong(0.0, 0.0)) {
            mvp.mapPosition = this.initialPosition
        }
        mvp.zoomLevelMax = zoomLevelMax
        mvp.zoomLevelMin = zoomLevelMin
        return mvp
    }


    /**
     * Creates the shared preferences that are being used to store map view data over
     * activity restarts.
     */
    private fun createSharedPreferences() {
        this.preferencesFacade = AndroidPreferences(this.getSharedPreferences(persistableId, Context.MODE_PRIVATE))
    }


    override fun onLocationChanged(location: Location) {
        this.myLocationOverlay?.setPosition(location.latitude, location.longitude, location.accuracy)
    }

    override fun onProviderDisabled(provider: String) {
    }

    override fun onProviderEnabled(provider: String) {
    }

    override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {
    }

    private fun enableAvailableProviders() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(Array(1) { Manifest.permission.ACCESS_FINE_LOCATION }, 0)
                return
            }
        }

        this.locationManager.removeUpdates(this)

        for (provider in this.locationManager.getProviders(true)) {
            if (LocationManager.GPS_PROVIDER == provider
                    || LocationManager.NETWORK_PROVIDER == provider) {
                this.locationManager.requestLocationUpdates(provider, 0, 0f, this)
            }
        }
    }

    fun showDetails(v: View) {
        val id = this.clusterer?.selectedItem?.id

        if (id != null) {
            val intent = Intent(this, RouteActivity::class.java)
            intent.putExtra(IntentConstants.ROCK_KEY, id)
            startActivity(intent)
        }
    }

    private fun download() {
        if (!NetworkUtils.isNetworkAvailable(this)) {
            Toast.makeText(this, "Keine Internetverbindung", Toast.LENGTH_LONG).show()
            return
        }
        FileDownloader(
                name = "Elevate4.zip",
                url = "https://www.openandromaps.org/wp-content/users/tobias/Elevate4.zip",
                context = this
        ).download { name: String ->
            unpackZip(getExternalFilesDir(null)?.path + File.separator, name)
        }
        FileDownloader(
                name = "sachsen.zip",
                url = "http://download.openandromaps.org/maps/Germany/sachsen.zip",
                context = this
        ).download { name: String ->
            unpackZip(getExternalFilesDir(null)?.path + File.separator, name)
        }
        FileDownloader(
                name = "Czech_Republic.zip",
                url = "https://ftp.gwdg.de/pub/misc/openstreetmap/openandromaps/maps/europe/Czech_Republic.zip",
                context = this
        ).download { name: String ->
            unpackZip(getExternalFilesDir(null)?.path + File.separator, name)
        }
        FileDownloader(
                name = "world.map",
                url = "http://download.mapsforge.org/maps/world/world.map",
                context = this
        ).download { }
    }

    /**
     * This MapViewer uses the built-in Osmarender theme.
     *
     * @return the render theme to use
     */
    private val renderTheme: XmlRenderTheme
        get() = try {
            ExternalRenderTheme(getExternalFilesDir(null)?.path + File.separator + "Elevate.xml")
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
            InternalRenderTheme.OSMARENDER
        }

    /**
     * This MapViewer uses the standard xml layout in the Samples app.
     */
    override fun getLayoutId(): Int {
        return R.layout.activity_map
    }

    /**
     * The id of the mapview inside the layout.
     *
     * @return the id of the MapView inside the layout.
     */
    private val mapViewId: Int
        get() = R.id.mapView


    private val mapFileDirectory: File?
        get() = getExternalFilesDir(null)

    private fun getPaint(color: Int, strokeWidth: Float, style: Style): Paint {
        val paint = AndroidGraphicFactory.INSTANCE.createPaint()
        paint.color = color
        paint.strokeWidth = strokeWidth
        paint.setStyle(style)
        return paint
    }

    /**
     * Creates a simple tile renderer layer with the AndroidUtil helper.
     */
    private fun createLayers() {

        val multiMapDataStore = MultiMapDataStore(MultiMapDataStore.DataPolicy.RETURN_ALL)

        try {
            multiMapDataStore.addMapDataStore(worldMapFile, true, true)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        try {
            multiMapDataStore.addMapDataStore(mapFile, false, false)
        } catch (e: MapFileException) {
            e.printStackTrace()
        }

        try {
            val czMapFile = MapFile(File(mapFileDirectory, "Czech_Republic.map"))
            multiMapDataStore.addMapDataStore(czMapFile, false, false)
        } catch (e: MapFileException) {
            e.printStackTrace()
        }

        val tileRendererLayer = AndroidUtil.createTileRendererLayer(this.tileCaches[0],
                this.mapView.model.mapViewPosition, multiMapDataStore, renderTheme, false, true, false)
        this.mapView.layerManager.layers.add(tileRendererLayer)

        val lat = this.intent.getDoubleExtra("lat", Double.NaN)
        val lon = this.intent.getDoubleExtra("lon", Double.NaN)

        val drawable = getDrawable(R.drawable.ic_my_location_black_24dp)
        val myLocation = Marker(null, AndroidGraphicFactory.convertToBitmap(drawable), 0, 0)

        val circle = Circle(null, 0f,
                getPaint(AndroidGraphicFactory.INSTANCE.createColor(48, 0, 0, 255), 0f, Style.FILL),
                getPaint(AndroidGraphicFactory.INSTANCE.createColor(160, 0, 0, 255), 2f, Style.STROKE))

        this.myLocationOverlay = MyLocationOverlay(myLocation, circle)
        this.mapView.layerManager.layers.add(this.myLocationOverlay)

        if (!isNaN(lat) && !isNaN(lon)) {
            val markerLocation = LatLong(lat, lon)
            this.mapView.setCenter(markerLocation)
            this.mapView.setZoomLevel(15.toByte())
        }

        if (clusterer == null) {
            clusterer = ClusterManager(
                    this,
                    mapView,
                    RockBitmapManager(this),
                    zoomLevelMax,
                    false)
        }
        // Create a Toast, see e.g. http://www.mkyong.com/android/android-toast-example/
        val toast = Toast.makeText(this, "", Toast.LENGTH_LONG)
        ClusterManager.toast = toast
        this.mapView.model.frameBufferModel.addObserver(clusterer)

        val geoItems = retrieveDbItems()

        // add geoitems for clustering
        for (i in geoItems) {
            clusterer?.addItem(i)
        }
    }

    /**
     * Creates the tile cache with the AndroidUtil helper
     */
    private fun createTileCaches() {
        this.tileCaches.add(AndroidUtil.createTileCache(this, persistableId,
                this.mapView.model.displayModel.tileSize, this.screenRatio,
                this.mapView.model.frameBufferModel.overdrawFactor))
    }
}

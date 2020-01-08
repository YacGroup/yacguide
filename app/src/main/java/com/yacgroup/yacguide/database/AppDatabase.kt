package com.example.paetz.yacguide.database

import android.arch.persistence.room.Database
import android.arch.persistence.room.Room
import android.arch.persistence.room.RoomDatabase
import android.arch.persistence.room.TypeConverters
import android.content.Context

import com.example.paetz.yacguide.database.Comment.RegionComment
import com.example.paetz.yacguide.database.Comment.RegionCommentDao
import com.example.paetz.yacguide.database.Comment.RockComment
import com.example.paetz.yacguide.database.Comment.RockCommentDao
import com.example.paetz.yacguide.database.Comment.RouteComment
import com.example.paetz.yacguide.database.Comment.RouteCommentDao
import com.example.paetz.yacguide.database.Comment.SectorComment
import com.example.paetz.yacguide.database.Comment.SectorCommentDao
import com.example.paetz.yacguide.utils.Converters

@Database(entities = [Country::class, Region::class, Sector::class, Rock::class, Route::class, Ascend::class, Partner::class, RegionComment::class, SectorComment::class, RockComment::class, RouteComment::class], version = 1)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun countryDao(): CountryDao
    abstract fun regionDao(): RegionDao
    abstract fun sectorDao(): SectorDao
    abstract fun rockDao(): RockDao
    abstract fun routeDao(): RouteDao
    abstract fun ascendDao(): AscendDao
    abstract fun partnerDao(): PartnerDao
    abstract fun regionCommentDao(): RegionCommentDao
    abstract fun sectorCommentDao(): SectorCommentDao
    abstract fun rockCommentDao(): RockCommentDao
    abstract fun routeCommentDao(): RouteCommentDao

    // Helpers to avoid code duplications
    fun deleteCountries() {
        val countries = countryDao().all
        for (country in countries) {
            deleteRegions(country.name)
        }
        countryDao().deleteAll()
    }

    fun deleteRegions(countryName: String) {
        val regions = regionDao().getAll(countryName)
        for (region in regions) {
            deleteSectors(region.id)
        }
        regionDao().deleteAll(countryName)
    }

    fun deleteSectors(regionId: Int) {
        val sectors = sectorDao().getAll(regionId)
        for (sector in sectors) {
            deleteRocks(sector.id)
        }
        sectorDao().deleteAll(regionId)
        regionCommentDao().deleteAll(regionId)
    }

    fun deleteRocks(sectorId: Int) {
        val rocks = rockDao().getAll(sectorId)
        for (rock in rocks) {
            val routes = routeDao().getAll(rock.id)
            for (route in routes) {
                routeCommentDao().deleteAll(route.id)
            }
            routeDao().deleteAll(rock.id)
            rockCommentDao().deleteAll(rock.id)
        }
        rockDao().deleteAll(sectorId)
        sectorCommentDao().deleteAll(sectorId)
    }

    fun deleteAscend(ascend: Ascend) {
        val routeId = ascend.routeId
        val ascendedCount = routeDao().getAscendCount(routeId) - 1
        routeDao().updateAscendCount(ascendedCount, routeId)
        if (ascendedCount == 0) {
            rockDao().updateAscended(false, routeDao().getRoute(routeId)!!.parentId)
        }
        ascendDao().delete(ascend)
    }

    // Some default stuff necessary for tourbook if according objects have
    // been deleted from the database
    fun createUnknownRoute(): Route {
        val route = Route()
        route.name = UNKNOWN_NAME
        route.grade = UNKNOWN_NAME
        return route
    }

    fun createUnknownRock(): Rock {
        val rock = Rock()
        rock.name = UNKNOWN_NAME
        return rock
    }

    fun createUnknownSector(): Sector {
        val sector = Sector()
        sector.name = UNKNOWN_NAME
        return sector
    }

    fun createUnknownRegion(): Region {
        val region = Region()
        region.name = UNKNOWN_NAME
        return region
    }

    companion object {

        private var _instance: AppDatabase? = null

        const val UNKNOWN_NAME = "???"
        const val INVALID_ID = -1

        fun getAppDatabase(context: Context): AppDatabase {
            synchronized(AppDatabase::class) {
                if (_instance == null) {
                    _instance = Room.databaseBuilder(context.applicationContext,
                            AppDatabase::class.java, "yac_database").allowMainThreadQueries().build()
                }
                return _instance as AppDatabase
            }
        }

        fun destroyInstance() {
            synchronized(AppDatabase::class) {
                _instance = null
            }
        }
    }
}

package com.example.paetz.yacguide.database;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;
import android.content.Context;

import com.example.paetz.yacguide.utils.Converters;
import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Database(entities = {Country.class, Region.class, Sector.class, Rock.class, Route.class, Comment.class, Ascend.class, Partner.class}, version = 1)
@TypeConverters({Converters.class})
public abstract class AppDatabase extends RoomDatabase {

    private static AppDatabase _instance;
    private static final String _UNKNOWN_NAME = "???";

    public static final int INVALID_ID = -1;

    public abstract CountryDao countryDao();
    public abstract RegionDao regionDao();
    public abstract SectorDao sectorDao();
    public abstract RockDao rockDao();
    public abstract RouteDao routeDao();
    public abstract CommentDao commentDao();
    public abstract AscendDao ascendDao();
    public abstract PartnerDao partnerDao();

    public final static BiMap<Integer, String> CLIMBING_STYLES;
    static {
        final Map<Integer, String> styles = new HashMap<Integer, String>();
        styles.put(1, "Onsight");
        styles.put(2, "Rotpunkt");
        styles.put(3, "Alles frei");
        styles.put(4, "Solo");
        styles.put(5, "Wechselführung");
        styles.put(6, "Nachstieg");
        CLIMBING_STYLES = ImmutableBiMap.copyOf(Collections.unmodifiableMap(styles));
    }

    public static AppDatabase getAppDatabase(Context context) {
        if (_instance == null) {
            _instance = Room.databaseBuilder(context.getApplicationContext(),
                    AppDatabase.class, "yac_database").allowMainThreadQueries().build();
        }
        return _instance;
    }

    public static void destroyInstance() {
        _instance = null;
    }

    // Helpers to avoid code duplications
    public void deleteCountries() {
        Country[] countries = countryDao().getAll();
        for (Country country : countries) {
            deleteRegions(country.getName());
        }
        countryDao().deleteAll();
    }

    public void deleteRegions(String countryName) {
        Region[] regions = regionDao().getAll(countryName);
        for (Region region : regions) {
            deleteSectors(region.getId());
        }
        regionDao().deleteAll(countryName);
    }

    public void deleteSectors(int regionId) {
        Sector[] sectors = sectorDao().getAll(regionId);
        for (Sector sector : sectors) {
            deleteRocks(sector.getId());
        }
        sectorDao().deleteAll(regionId);
    }

    public void deleteRocks(int sectorId) {
        Rock[] rocks = rockDao().getAll(sectorId);
        for (Rock rock : rocks) {
            Route[] routes = routeDao().getAll(rock.getId());
            for (Route route : routes) {
                commentDao().deleteAll(route.getId());
            }
            routeDao().deleteAll(rock.getId());
        }
        rockDao().deleteAll(sectorId);
    }

    // Some default stuff necessary for tourbook if according objects have
    // been deleted from the database
    public Route createUnknownRoute() {
        Route route = new Route();
        route.setName(_UNKNOWN_NAME);
        route.setGrade(_UNKNOWN_NAME);
        return route;
    }
    public Rock createUnknownRock() {
        Rock rock = new Rock();
        rock.setName(_UNKNOWN_NAME);
        return rock;
    }
    public Sector createUnknownSector() {
        Sector sector = new Sector();
        sector.setName(_UNKNOWN_NAME);
        return sector;
    }
    public Region createUnknownRegion() {
        Region region = new Region();
        region.setName(_UNKNOWN_NAME);
        return region;
    }
}

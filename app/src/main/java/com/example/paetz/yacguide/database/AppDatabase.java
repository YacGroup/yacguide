package com.example.paetz.yacguide.database;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;
import android.content.Context;

import com.example.paetz.yacguide.database.Comment.RegionComment;
import com.example.paetz.yacguide.database.Comment.RegionCommentDao;
import com.example.paetz.yacguide.database.Comment.RockComment;
import com.example.paetz.yacguide.database.Comment.RockCommentDao;
import com.example.paetz.yacguide.database.Comment.RouteComment;
import com.example.paetz.yacguide.database.Comment.RouteCommentDao;
import com.example.paetz.yacguide.database.Comment.SectorComment;
import com.example.paetz.yacguide.database.Comment.SectorCommentDao;
import com.example.paetz.yacguide.utils.Converters;

@Database(entities = {Country.class, Region.class, Sector.class, Rock.class, Route.class, Ascend.class, Partner.class,
        RegionComment.class, SectorComment.class, RockComment.class, RouteComment.class}, version = 1)
@TypeConverters({Converters.class})
public abstract class AppDatabase extends RoomDatabase {

    private static AppDatabase _instance;

    public static final String UNKNOWN_NAME = "???";
    public static final int INVALID_ID = -1;

    public abstract CountryDao countryDao();
    public abstract RegionDao regionDao();
    public abstract SectorDao sectorDao();
    public abstract RockDao rockDao();
    public abstract RouteDao routeDao();
    public abstract AscendDao ascendDao();
    public abstract PartnerDao partnerDao();
    public abstract RegionCommentDao regionCommentDao();
    public abstract SectorCommentDao sectorCommentDao();
    public abstract RockCommentDao rockCommentDao();
    public abstract RouteCommentDao routeCommentDao();

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
        final Country[] countries = countryDao().getAll();
        for (final Country country : countries) {
            deleteRegions(country.getName());
        }
        countryDao().deleteAll();
    }

    public void deleteRegions(String countryName) {
        final Region[] regions = regionDao().getAll(countryName);
        for (final Region region : regions) {
            deleteSectors(region.getId());
        }
        regionDao().deleteAll(countryName);
    }

    public void deleteSectors(int regionId) {
        final Sector[] sectors = sectorDao().getAll(regionId);
        for (final Sector sector : sectors) {
            deleteRocks(sector.getId());
        }
        sectorDao().deleteAll(regionId);
        regionCommentDao().deleteAll(regionId);
    }

    public void deleteRocks(int sectorId) {
        final Rock[] rocks = rockDao().getAll(sectorId);
        for (final Rock rock : rocks) {
            final Route[] routes = routeDao().getAll(rock.getId());
            for (final Route route : routes) {
                routeCommentDao().deleteAll(route.getId());
            }
            routeDao().deleteAll(rock.getId());
            rockCommentDao().deleteAll(rock.getId());
        }
        rockDao().deleteAll(sectorId);
        sectorCommentDao().deleteAll(sectorId);
    }

    // Some default stuff necessary for tourbook if according objects have
    // been deleted from the database
    public Route createUnknownRoute() {
        Route route = new Route();
        route.setName(UNKNOWN_NAME);
        route.setGrade(UNKNOWN_NAME);
        return route;
    }
    public Rock createUnknownRock() {
        Rock rock = new Rock();
        rock.setName(UNKNOWN_NAME);
        return rock;
    }
    public Sector createUnknownSector() {
        Sector sector = new Sector();
        sector.setName(UNKNOWN_NAME);
        return sector;
    }
    public Region createUnknownRegion() {
        Region region = new Region();
        region.setName(UNKNOWN_NAME);
        return region;
    }
}

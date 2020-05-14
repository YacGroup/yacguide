/*
 * Copyright (C) 2019 Fabian Kantereit
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.yacgroup.yacguide.database

import android.arch.persistence.room.Database
import android.arch.persistence.room.Room
import android.arch.persistence.room.RoomDatabase
import android.arch.persistence.room.TypeConverters
import android.content.Context

import com.yacgroup.yacguide.database.Comment.RegionComment
import com.yacgroup.yacguide.database.Comment.RegionCommentDao
import com.yacgroup.yacguide.database.Comment.RockComment
import com.yacgroup.yacguide.database.Comment.RockCommentDao
import com.yacgroup.yacguide.database.Comment.RouteComment
import com.yacgroup.yacguide.database.Comment.RouteCommentDao
import com.yacgroup.yacguide.database.Comment.SectorComment
import com.yacgroup.yacguide.database.Comment.SectorCommentDao
import com.yacgroup.yacguide.utils.AscendStyle
import com.yacgroup.yacguide.utils.Converters

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
        countryDao().all.map{ deleteRegions(it.name) }
        countryDao().deleteAll()
    }

    fun deleteRegions(countryName: String) {
        regionDao().getAll(countryName).map{ deleteSectors(it.id) }
        regionDao().deleteAll(countryName)
    }

    fun deleteSectors(regionId: Int) {
        sectorDao().getAll(regionId).map{ deleteRocks(it.id) }
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

    fun deleteAscends() {
        for (rock in rockDao().all) {
            rockDao().setAscendsBitMask(bitMask = 0, id = rock.id)
        }
        for (route in routeDao().all) {
            routeDao().setAscendsBitMask(bitMask = 0, id = route.id)
        }
        for (ascend in ascendDao().all) {
            ascendDao().delete(ascend)
        }
    }

    fun deleteAscend(ascend: Ascend) {
        uncheckBitMasks(ascend)
        ascendDao().delete(ascend)
    }

    fun checkBitMasks(ascend: Ascend) {
        val route = routeDao().getRoute(ascend.routeId)
        if (route != null) {
            routeDao().setAscendsBitMask(route.ascendsBitMask or AscendStyle.bitMask(ascend.styleId), route.id)
            rockDao().setAscendsBitMask(rockDao().getRock(route.parentId)!!.ascendsBitMask or AscendStyle.bitMask(ascend.styleId), route.parentId)
        }
    }

    fun uncheckBitMasks(ascend: Ascend) {
        val route = routeDao().getRoute(ascend.routeId)
        if (route != null) {
            val rock = rockDao().getRockForAscend(ascend.id)
            val rockAscends = ascendDao().getAscendsForRock(rock!!.id)
            var rockFound = false
            var routeFound = false
            for (rockAscend in rockAscends) {
                if (ascend.id == rockAscend.id) {
                    continue
                }
                if (ascend.styleId == rockAscend.styleId) {
                    rockFound = true
                    if (ascend.routeId == rockAscend.routeId) {
                        routeFound = true
                        break
                    }
                }
            }
            if (!rockFound) {
                rockDao().setAscendsBitMask(rock.ascendsBitMask and AscendStyle.bitMask(ascend.styleId).inv(), rock.id)
            }
            if (!routeFound) {
                routeDao().setAscendsBitMask(route.ascendsBitMask and AscendStyle.bitMask(ascend.styleId).inv(), route.id)
            }
        }
    }

    // Some default stuff necessary for import_export if according objects have
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
    }
}

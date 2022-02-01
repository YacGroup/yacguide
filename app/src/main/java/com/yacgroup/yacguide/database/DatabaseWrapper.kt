/*
 * Copyright (C) 2020 Axel Paetzold
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

import android.content.Context
import com.yacgroup.yacguide.database.comment.RegionComment
import com.yacgroup.yacguide.database.comment.RockComment
import com.yacgroup.yacguide.database.comment.RouteComment
import com.yacgroup.yacguide.database.comment.SectorComment
import com.yacgroup.yacguide.utils.AscendStyle

class DatabaseWrapper(context: Context) {

    companion object {
        const val UNKNOWN_NAME = "???"
        const val INVALID_ID = -1
    }

    private var _db: AppDatabase = AppDatabase.getAppDatabase(context)

    private inline fun<Unit> _dbTransaction(crossinline func: () -> Unit) {
        _db.runInTransaction {
            func()
        }
    }

    // Getters
    // This is a cheap db transaction, so we skip the transaction decorator for this group

    fun getCountries() = _db.countryDao().all

    fun getNonEmptyCountries() = _db.countryDao().getAllNonEmpty()

    fun getRegions(countryName: String) = _db.regionDao().getAll(countryName)

    fun getNonEmptyRegions() = _db.regionDao().getAllNonEmpty()

    fun getRegion(regionId: Int) = _db.regionDao().getRegion(regionId)

    fun getRegionComments(regionId: Int) = _db.regionCommentDao().getAll(regionId)

    fun getSectors(regionId: Int) = _db.sectorDao().getAll(regionId)

    fun getSector(sectorId: Int) = _db.sectorDao().getSector(sectorId)

    fun getSectorComments(sectorId: Int) = _db.sectorCommentDao().getAll(sectorId)

    fun getRocksForSector(sectorId: Int) = _db.rockDao().getAllInSector(sectorId)

    fun getRocksByNameForSector(sectorId: Int, namePart: String) = _db.rockDao().getAllByNameInSector(sectorId, "%$namePart%")

    fun getRocksForRegion(regionId: Int) = _db.rockDao().getAllInRegion(regionId)

    fun getRocksByNameForRegion(regionId: Int, namePart: String) = _db.rockDao().getAllByNameInRegion(regionId, "%$namePart%")

    fun getRocksForCountry(countryName: String) = _db.rockDao().getAllInCountry(countryName)

    fun getRocksByNameForCountry(countryName: String, namePart: String) = _db.rockDao().getAllByNameInCountry(countryName, "%$namePart%")

    fun getRocks() = _db.rockDao().all

    fun getRocksByName(namePart: String) = _db.rockDao().getAllByName("%$namePart%")

    fun getRock(rockId: Int) =_db.rockDao().getRock(rockId)

    fun getRockComments(rockId: Int) = _db.rockCommentDao().getAll(rockId)

    fun getRoutesForRock(rockId: Int) = _db.routeDao().getAllAtRock(rockId)

    fun getProjectedRoutesForRock(rockId: Int) = _db.routeDao().getAllAtRockForStyle(rockId, AscendStyle.ePROJECT.id)

    fun getBotchedRoutesForRock(rockId: Int) = _db.routeDao().getAllAtRockForStyle(rockId, AscendStyle.eBOTCHED.id)

    fun getRoutesForSector(sectorId: Int) = _db.routeDao().getAllInSector(sectorId)

    fun getProjectedRoutesForSector(sectorId: Int) = _db.routeDao().getAllInSectorForStyle(sectorId, AscendStyle.ePROJECT.id)

    fun getBotchedRoutesForSector(sectorId: Int) = _db.routeDao().getAllInSectorForStyle(sectorId, AscendStyle.eBOTCHED.id)

    fun getRoutesForRegion(regionId: Int) = _db.routeDao().getAllInRegion(regionId)

    fun getProjectedRoutesForRegion(regionId: Int) = _db.routeDao().getAllInRegionForStyle(regionId, AscendStyle.ePROJECT.id)

    fun getBotchedRoutesForRegion(regionId: Int) = _db.routeDao().getAllInRegionForStyle(regionId, AscendStyle.eBOTCHED.id)

    fun getRoutesForCountry(countryName: String) = _db.routeDao().getAllInCountry(countryName)

    fun getProjectedRoutesForCountry(countryName: String) = _db.routeDao().getAllInCountryForStyle(countryName, AscendStyle.ePROJECT.id)

    fun getBotchedRoutesForCountry(countryName: String) = _db.routeDao().getAllInCountryForStyle(countryName, AscendStyle.eBOTCHED.id)

    fun getRoutes() = _db.routeDao().all

    fun getProjectedRoutes() = _db.routeDao().getAllForStyle(AscendStyle.ePROJECT.id)

    fun getBotchedRoutes() = _db.routeDao().getAllForStyle(AscendStyle.eBOTCHED.id)

    fun getRoute(routeId: Int) = _db.routeDao().getRoute(routeId)

    fun getRouteComments(routeId: Int) = _db.routeCommentDao().getAll(routeId)

    fun getRouteCommentCount(routeId: Int) = _db.routeCommentDao().getCommentCount(routeId)

    fun getAscends() = _db.ascendDao().all

    fun getAscend(ascendId: Int) = _db.ascendDao().getAscend(ascendId)

    fun getRouteAscends(routeId: Int) = _db.ascendDao().getAscendsForRoute(routeId)

    fun getRockAscends(rockId: Int) = _db.ascendDao().getAscendsForRock(rockId)

    fun getAscendsOfStyle(year: Int, styleId: Int) = _db.ascendDao().getAll(year, styleId)

    fun getAscendsBelowStyleId(year: Int, styleId: Int) = _db.ascendDao().getAllBelowStyleId(year, styleId)

    fun getYearsOfStyle(styleId: Int) = _db.ascendDao().getYears(styleId)

    fun getYearsBelowStyleId(styleId: Int) = _db.ascendDao().getYearsBelowStyleId(styleId)

    fun getPartners() = _db.partnerDao().all

    fun getPartnerId(partnerName: String) = _db.partnerDao().getId(partnerName)

    fun getPartnerIds(partnerNames: List<String>) = partnerNames.map { _db.partnerDao().getId(it) }

    fun getPartnerNames(partnerIds: List<Int>) = partnerIds.map { _db.partnerDao().getPartner(it)?.name ?: UNKNOWN_NAME }

    // Insertions

    fun addCountries(countries: List<Country>) = _dbTransaction {
        _db.countryDao().insert(countries)
    }

    fun addRegions(regions: List<Region>) = _dbTransaction {
        _db.regionDao().insert(regions)
    }

    fun addSectors(sectors: List<Sector>) = _dbTransaction {
        _db.sectorDao().insert(sectors)
    }

    fun addRocks(rocks: List<Rock>) = _dbTransaction {
        _db.rockDao().insert(rocks)
    }

    fun addRoutes(routes: List<Route>) = _dbTransaction {
        _db.routeDao().insert(routes)
    }

    fun addRegionComments(comments: List<RegionComment>) = _dbTransaction {
        _db.regionCommentDao().insert(comments)
    }

    fun addSectorComments(comments: List<SectorComment>) = _dbTransaction {
        _db.sectorCommentDao().insert(comments)
    }

    fun addRockComments(comments: List<RockComment>) = _dbTransaction {
        _db.rockCommentDao().insert(comments)
    }

    fun addRouteComments(comments: List<RouteComment>) = _dbTransaction {
        _db.routeCommentDao().insert(comments)
    }

    fun addAscend(ascend: Ascend) = _dbTransaction {
        _db.ascendDao().insert(ascend)
        _checkBitMasks(ascend)
    }

    fun addAscends(ascends: List<Ascend>) = _dbTransaction {
        _db.ascendDao().insert(ascends)
        ascends.map { _checkBitMasks(it) }
    }

    fun addPartner(partner: Partner) = _dbTransaction {
        _db.partnerDao().insert(partner)
    }

    fun addPartners(partners: List<Partner>) = _dbTransaction {
        _db.partnerDao().insert(partners)
    }

    // Deletions

    fun deleteCountries() = _dbTransaction {
        _db.countryDao().deleteAll()
    }

    fun deleteRegions(countryName: String) = _dbTransaction {
        _db.regionDao().deleteAll(countryName)
    }

    fun deleteCountriesRecursively() = _dbTransaction {
        _db.routeCommentDao().deleteAll()
        _db.routeDao().deleteAll()

        _db.rockCommentDao().deleteAll()
        _db.rockDao().deleteAll()

        _db.sectorCommentDao().deleteAll()
        _db.sectorDao().deleteAll()

        _db.regionCommentDao().deleteAll()
        _db.regionDao().deleteAll()

        _db.countryDao().deleteAll()
    }

    fun deleteSectorsRecursively(regionId: Int) = _dbTransaction {
        val routeComments = _db.routeCommentDao().getAllInRegion(regionId)
        _db.routeCommentDao().delete(routeComments)
        val routes = _db.routeDao().getAllInRegion(regionId)
        _db.routeDao().delete(routes)

        val rockComments = _db.rockCommentDao().getAllInRegion(regionId)
        _db.rockCommentDao().delete(rockComments)
        val rocks = _db.rockDao().getAllInRegion(regionId)
        _db.rockDao().delete(rocks)

        val sectorComments = _db.sectorCommentDao().getAllInRegion(regionId)
        _db.sectorCommentDao().delete(sectorComments)
        _db.sectorDao().deleteAll(regionId)

        _db.regionCommentDao().deleteAll(regionId)
    }

    fun deleteAscends() = _dbTransaction {
        val rocks = _db.rockDao().all
        for (rock in rocks) {
            rock.ascendsBitMask = 0
        }
        _db.rockDao().update(rocks)

        val routes = _db.routeDao().all
        for (route in routes) {
            route.ascendsBitMask = 0
        }
        _db.routeDao().update(routes)

        _db.ascendDao().deleteAll()
    }

    fun deleteAscend(ascend: Ascend) = _dbTransaction {
        _uncheckBitMasks(ascend)
        _db.ascendDao().delete(ascend)
    }

    fun deletePartners() = _dbTransaction {
        _db.partnerDao().deleteAll()
    }

    fun deletePartner(partner: Partner) = _dbTransaction {
        _db.partnerDao().delete(partner)
    }

    // Updates

    fun updateRocks(rocks: List<Rock>) = _dbTransaction {
        _db.rockDao().update(rocks)
    }

    private fun _checkBitMasks(ascend: Ascend) = _dbTransaction {
        val route = _db.routeDao().getRoute(ascend.routeId)
        if (route != null) {
            route.ascendsBitMask = route.ascendsBitMask or AscendStyle.bitMask(ascend.styleId)
            _db.routeDao().update(route)

            val rock = _db.rockDao().getRock(route.parentId)!!
            rock.ascendsBitMask = rock.ascendsBitMask or AscendStyle.bitMask(ascend.styleId)
            _db.rockDao().update(rock)
        }
    }

    private fun _uncheckBitMasks(ascend: Ascend) = _dbTransaction {
        val route = _db.routeDao().getRoute(ascend.routeId)
        if (route != null) {
            val rock = _db.rockDao().getRockForAscend(ascend.id)
            val rockAscends = _db.ascendDao().getAscendsForRock(rock!!.id)
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
                rock.ascendsBitMask = rock.ascendsBitMask and AscendStyle.bitMask(ascend.styleId).inv()
                _db.rockDao().update(rock)
            }
            if (!routeFound) {
                route.ascendsBitMask = route.ascendsBitMask and AscendStyle.bitMask(ascend.styleId).inv()
                _db.routeDao().update(route)
            }
        }
    }

    // Some default stuff necessary for import_export if according objects have
    // been deleted from the database
    fun createUnknownRoute(): Route {
        return Route().apply {
            name = UNKNOWN_NAME
            grade = UNKNOWN_NAME
        }
    }

    fun createUnknownRock(): Rock {
        return Rock().apply {
            name = UNKNOWN_NAME
        }
    }

    fun createUnknownSector(): Sector {
        return Sector().apply {
            name = UNKNOWN_NAME
        }
    }

    fun createUnknownRegion(): Region {
        return Region(
            id = INVALID_ID,
            name = UNKNOWN_NAME,
            country = UNKNOWN_NAME
        )
    }

}
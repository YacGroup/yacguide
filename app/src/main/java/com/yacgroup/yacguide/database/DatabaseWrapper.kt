/*
 * Copyright (C) 2020, 2022, 2023, 2025 Axel Paetzold
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

    fun getRegions(countryName: String) = _db.regionDao().getAllInCountry(countryName)

    fun getNonEmptyRegions() = _db.regionDao().getAllNonEmpty()

    fun getRegion(regionId: Int) = _db.regionDao().getRegion(regionId)

    fun getRegionForRoute(routeId: Int) = _db.regionDao().getRegionForRoute(routeId)

    fun getRegionComments(regionId: Int) = _db.regionCommentDao().getAll(regionId)

    fun getSectors(regionId: Int) = _db.sectorDao().getAllInRegion(regionId)

    fun getSector(sectorId: Int) = _db.sectorDao().getSector(sectorId)

    fun getSectorComments(sectorId: Int) = _db.sectorCommentDao().getAll(sectorId)

    fun getRocksForSector(sectorId: Int) = _db.rockDao().getAllInSector(sectorId)

    fun getRocksByNameForSector(sectorId: Int, namePart: String) = _db.rockDao().getAllByNameInSector(sectorId, "%$namePart%")

    fun getRocksByRelevanceForSector(sectorId: Int, maxRelevanceId: Int) = _db.rockDao().getAllByRelevanceInSector(sectorId, maxRelevanceId)

    fun getRocksForRegion(regionId: Int) = _db.rockDao().getAllInRegion(regionId)

    fun getRocksByNameForRegion(regionId: Int, namePart: String) = _db.rockDao().getAllByNameInRegion(regionId, "%$namePart%")

    fun getRocksByRelevanceForRegion(regionId: Int, maxRelevanceId: Int) = _db.rockDao().getAllByRelevanceInRegion(regionId, maxRelevanceId)

    fun getRocksForCountry(countryName: String) = _db.rockDao().getAllInCountry(countryName)

    fun getRocksByNameForCountry(countryName: String, namePart: String) = _db.rockDao().getAllByNameInCountry(countryName, "%$namePart%")

    fun getRocksByRelevanceForCountry(countryName: String, maxRelevanceId: Int) = _db.rockDao().getAllByRelevanceInCountry(countryName, maxRelevanceId)

    fun getRocks() = _db.rockDao().all

    fun getRocksByName(namePart: String) = _db.rockDao().getAllByName("%$namePart%")

    fun getRocksByRelevance(maxRelevanceId: Int) = _db.rockDao().getAllByRelevance(maxRelevanceId)

    fun getRock(rockId: Int) =_db.rockDao().getRock(rockId)

    fun getRockComments(rockId: Int) = _db.rockCommentDao().getAll(rockId)

    fun getRoutesForRock(rockId: Int) = _db.routeDao().getAllAtRock(rockId)

    fun getRoutesByNameForRock(rockId: Int, namePart: String) = _db.routeDao().getAllByNameAtRock(rockId, "%$namePart%")

    fun getRoutesByGradeForRock(rockId: Int, minGradeId: Int, maxGradeId: Int) = _db.routeDao().getAllByGradeAtRock(rockId, minGradeId, maxGradeId)

    fun getRoutesByQualityForRock(rockId: Int, maxQualityId: Int) = _db.routeDao().getAllByQualityAtRock(rockId, maxQualityId)

    fun getRoutesByProtectionForRock(rockId: Int, maxProtectionId: Int) = _db.routeDao().getAllByProtectionAtRock(rockId, maxProtectionId)

    fun getRoutesByDryingForRock(rockId: Int, maxDryingId: Int) = _db.routeDao().getAllByDryingAtRock(rockId, maxDryingId)

    fun getProjectedRoutesForRock(rockId: Int) = _db.routeDao().getAllAtRockForStyle(rockId, AscendStyle.ePROJECT.id)

    fun getBotchedRoutesForRock(rockId: Int) = _db.routeDao().getAllAtRockForStyle(rockId, AscendStyle.eBOTCHED.id)

    fun getRoutesForSector(sectorId: Int) = _db.routeDao().getAllInSector(sectorId)

    fun getRoutesByNameForSector(sectorId: Int, namePart: String) = _db.routeDao().getAllByNameInSector(sectorId, "%$namePart%")

    fun getRoutesByGradeForSector(sectorId: Int, minGradeId: Int, maxGradeId: Int) = _db.routeDao().getAllByGradeInSector(sectorId, minGradeId, maxGradeId)

    fun getRoutesByQualityForSector(sectorId: Int, maxQualityId: Int) = _db.routeDao().getAllByQualityInSector(sectorId, maxQualityId)

    fun getRoutesByProtectionForSector(sectorId: Int, maxProtectionId: Int) = _db.routeDao().getAllByProtectionInSector(sectorId, maxProtectionId)

    fun getRoutesByDryingForSector(sectorId: Int, maxDryingId: Int) = _db.routeDao().getAllByDryingInSector(sectorId, maxDryingId)

    fun getProjectedRoutesForSector(sectorId: Int) = _db.routeDao().getAllInSectorForStyle(sectorId, AscendStyle.ePROJECT.id)

    fun getBotchedRoutesForSector(sectorId: Int) = _db.routeDao().getAllInSectorForStyle(sectorId, AscendStyle.eBOTCHED.id)

    fun getRoutesForRegion(regionId: Int) = _db.routeDao().getAllInRegion(regionId)

    fun getRoutesByNameForRegion(regionId: Int, namePart: String) = _db.routeDao().getAllByNameInRegion(regionId, "%$namePart%")

    fun getRoutesByGradeForRegion(regionId: Int, minGradeId: Int, maxGradeId: Int) = _db.routeDao().getAllByGradeInRegion(regionId, minGradeId, maxGradeId)

    fun getRoutesByQualityForRegion(regionId: Int, maxQualityId: Int) = _db.routeDao().getAllByQualityInRegion(regionId, maxQualityId)

    fun getRoutesByProtectionForRegion(regionId: Int, maxProtectionId: Int) = _db.routeDao().getAllByProtectionInRegion(regionId, maxProtectionId)

    fun getRoutesByDryingForRegion(regionId: Int, maxDryingId: Int) = _db.routeDao().getAllByDryingInRegion(regionId, maxDryingId)

    fun getProjectedRoutesForRegion(regionId: Int) = _db.routeDao().getAllInRegionForStyle(regionId, AscendStyle.ePROJECT.id)

    fun getBotchedRoutesForRegion(regionId: Int) = _db.routeDao().getAllInRegionForStyle(regionId, AscendStyle.eBOTCHED.id)

    fun getRoutesForCountry(countryName: String) = _db.routeDao().getAllInCountry(countryName)

    fun getRoutesByNameForCountry(countryName: String, namePart: String) = _db.routeDao().getAllByNameInCountry(countryName, "%$namePart%")

    fun getRoutesByGradeForCountry(countryName: String, minGradeId: Int, maxGradeId: Int) = _db.routeDao().getAllByGradeInCountry(countryName, minGradeId, maxGradeId)

    fun getRoutesByQualityForCountry(countryName: String, maxQualityId: Int) = _db.routeDao().getAllByQualityInCountry(countryName, maxQualityId)

    fun getRoutesByProtectionForCountry(countryName: String, maxProtectionId: Int) = _db.routeDao().getAllByProtectionInCountry(countryName, maxProtectionId)

    fun getRoutesByDryingForCountry(countryName: String, maxDryingId: Int) = _db.routeDao().getAllByDryingInCountry(countryName, maxDryingId)

    fun getProjectedRoutesForCountry(countryName: String) = _db.routeDao().getAllInCountryForStyle(countryName, AscendStyle.ePROJECT.id)

    fun getBotchedRoutesForCountry(countryName: String) = _db.routeDao().getAllInCountryForStyle(countryName, AscendStyle.eBOTCHED.id)

    fun getRoutes() = _db.routeDao().all

    fun getRoutesByName(namePart: String) = _db.routeDao().getAllByName("%$namePart%")

    fun getRoutesByGrade(minGradeId: Int, maxGradeId: Int) = _db.routeDao().getAllByGrade(minGradeId, maxGradeId)

    fun getRoutesByQuality(maxQualityId: Int) = _db.routeDao().getAllByQuality(maxQualityId)

    fun getRoutesByProtection(maxProtectionId: Int) = _db.routeDao().getAllByProtection(maxProtectionId)

    fun getRoutesByDrying(maxDryingId: Int) = _db.routeDao().getAllByDrying(maxDryingId)

    fun getProjectedRoutes() = _db.routeDao().getAllForStyle(AscendStyle.ePROJECT.id)

    fun getBotchedRoutes() = _db.routeDao().getAllForStyle(AscendStyle.eBOTCHED.id)

    fun getRoute(routeId: Int) = _db.routeDao().getRoute(routeId)

    fun getRouteComments(routeId: Int) = _db.routeCommentDao().getAll(routeId)

    fun getRouteCommentCount(routeId: Int) = _db.routeCommentDao().getCommentCount(routeId)

    fun getAscends() = _db.ascendDao().all

    fun getAscend(ascendId: Int) = _db.ascendDao().getAscend(ascendId)

    fun getRouteAscends(routeId: Int) = _db.ascendDao().getAscendsForRoute(routeId)

    fun getRockAscends(rockId: Int) = _db.ascendDao().getAscendsForRock(rockId)

    fun getAscendsBelowStyleId(styleId: Int) = _db.ascendDao().getAscendsBelowStyleId(styleId)

    fun getAscendsOfYearAndStyle(year: Int, styleId: Int) = _db.ascendDao().getAscendsForYearAndStyle(year, styleId)

    fun getAscendsOfYearBelowStyleId(year: Int, styleId: Int) = _db.ascendDao().getAscendsForYearBelowStyleId(year, styleId)

    fun getYearsOfStyle(styleId: Int) = _db.ascendDao().getYears(styleId)

    fun getYearsBelowStyleId(styleId: Int) = _db.ascendDao().getYearsBelowStyleId(styleId)

    fun getAscendCountPerYear(startStyleId: Int, endStyleId: Int = startStyleId) = _db.ascendDao().getAscendCountPerYear(startStyleId, endStyleId)

    fun getNewlyAscendedRockCountsPerYear(startStyleId: Int, endStyleId: Int, rockTypes: List<Char>, excludedRockStates: List<Char>) = _db.ascendDao().getNewlyAscendedRockCountsPerYear(startStyleId, endStyleId, rockTypes, excludedRockStates)

    fun getPartners() = _db.partnerDao().all

    fun getPartner(partnerId: Int) = _db.partnerDao().getPartner(partnerId)

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

    fun deleteRegions() = _dbTransaction {
        _db.regionDao().deleteAll()
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
        return Route(
            id = INVALID_ID,
            nr = 0f,
            statusId = 0,
            name = UNKNOWN_NAME,
            grade = UNKNOWN_NAME,
            firstAscendLeader = UNKNOWN_NAME,
            firstAscendFollower = UNKNOWN_NAME,
            firstAscendDate = UNKNOWN_NAME,
            typeOfClimbing = UNKNOWN_NAME,
            description = UNKNOWN_NAME,
            ascendsBitMask = 0,
            parentId = INVALID_ID
        )
    }

    fun createUnknownRock(): Rock {
        return Rock(
            id = INVALID_ID,
            nr = 0f,
            type = ' ',
            status = ' ',
            name = UNKNOWN_NAME,
            longitude = 0f,
            latitude = 0f,
            ascendsBitMask = 0,
            parentId = INVALID_ID
        )
    }

    fun createUnknownSector(): Sector {
        return Sector(
            id = INVALID_ID,
            nr = 0f,
            name = UNKNOWN_NAME,
            parentId = INVALID_ID
        )
    }

    fun createUnknownRegion(): Region {
        return Region(
            id = INVALID_ID,
            name = UNKNOWN_NAME,
            country = UNKNOWN_NAME
        )
    }

}
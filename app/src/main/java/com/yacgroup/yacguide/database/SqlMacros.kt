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

class SqlMacros {

    companion object {

        // selects
        const val SELECT_COUNTRIES = "SELECT DISTINCT Country.* FROM Country"
        const val SELECT_REGIONS = "SELECT DISTINCT Region.* FROM Region"
        const val SELECT_REGION_COMMENTS = "SELECT DISTINCT RegionComment.* FROM RegionComment"
        const val SELECT_SECTORS = "SELECT DISTINCT Sector.* FROM Sector"
        const val SELECT_SECTOR_COMMENTS = "SELECT DISTINCT SectorComment.* FROM SectorComment"
        const val SELECT_ROCKS = "SELECT DISTINCT Rock.* FROM Rock"
        const val SELECT_ROCK_COMMENTS = "SELECT DISTINCT RockComment.* FROM RockComment"
        const val SELECT_ROUTES = "SELECT DISTINCT Route.* FROM Route"
        const val SELECT_ROUTE_COMMENTS = "SELECT DISTINCT RouteComment.* FROM RouteComment"
        const val SELECT_ASCENDS = "SELECT DISTINCT Ascend.* FROM Ascend"
        const val SELECT_ASCENDS_YEARS = "SELECT DISTINCT Ascend.year FROM Ascend"
        const val SELECT_PARTNERS = "SELECT DISTINCT Partner.* FROM Partner"
        const val SELECT_PARTNER_ID = "SELECT Partner.Id FROM Partner"

        // counts
        const val COUNT_ROUTE_COMMENTS = "SELECT COUNT(*) FROM RouteComment"

        // deletions
        const val DELETE_COUNTRIES = "DELETE FROM Country"
        const val DELETE_REGIONS = "DELETE FROM Region"
        const val DELETE_REGION_COMMENTS = "DELETE FROM RegionComment"
        const val DELETE_SECTORS = "DELETE FROM Sector"
        const val DELETE_SECTOR_COMMENTS = "DELETE FROM SectorComment"
        const val DELETE_ROCKS = "DELETE FROM Rock"
        const val DELETE_ROCK_COMMENTS = "DELETE FROM RockComment"
        const val DELETE_ROUTES = "DELETE FROM Route"
        const val DELETE_ROUTE_COMMENTS = "DELETE FROM RouteComment"
        const val DELETE_ASCENDS = "DELETE FROM Ascend"
        const val DELETE_PARTNERS = "DELETE FROM Partner"

        // orderings
        const val ORDERED_BY_REGION_SECTOR_ROCK = "ORDER BY Region.id, Sector.nr, Rock.nr"
        const val ORDERED_BY_SECTOR_ROCK = "ORDER BY Sector.nr, Rock.nr"
        const val ORDERED_BY_SECTOR = "ORDER BY Sector.nr"
        const val ORDERED_BY_ROCK = "ORDER BY Rock.nr"
        const val ORDERED_BY_ROUTE = "ORDER BY Route.nr"
        const val ORDERED_BY_DATE = "ORDER BY Ascend.year, Ascend.month, Ascend.day"

        // joins
        const val VIA_ROUTES_ASCENDS = "JOIN Ascend ON Ascend.routeId = Route.id"
        const val VIA_ROCKS_ROUTES = "JOIN Route ON Route.parentId = Rock.id"
        const val VIA_ASCENDS_ROUTE = "JOIN Route ON Route.id = Ascend.routeId"
        const val VIA_COMMENTS_ROUTE = "JOIN Route ON Route.id = RouteComment.routeId"
        const val VIA_ROUTES_ROCK = "JOIN Rock ON Rock.id = Route.parentId"
        const val VIA_COMMENTS_ROCK = "JOIN Rock ON Rock.id = RockComment.rockId"
        const val VIA_ROCKS_SECTOR = "JOIN Sector ON Sector.id = Rock.parentId"
        const val VIA_COMMENTS_SECTOR = "JOIN Sector ON Sector.id = SectorComment.sectorId"
        const val VIA_SECTORS_REGION = "JOIN Region ON Region.id = Sector.parentId"
        const val VIA_COMMENTS_REGION = "JOIN Region ON Region.id = RegionComment.regionId"

        // multi-level joins
        const val VIA_ROCKS_ASCENDS = "$VIA_ROCKS_ROUTES $VIA_ROUTES_ASCENDS"
        const val VIA_ROCKS_REGION = "$VIA_ROCKS_SECTOR $VIA_SECTORS_REGION"
        const val VIA_ROUTES_SECTOR = "$VIA_ROUTES_ROCK $VIA_ROCKS_SECTOR"
        const val VIA_ROUTES_REGION = "$VIA_ROUTES_SECTOR $VIA_SECTORS_REGION"
    }
}
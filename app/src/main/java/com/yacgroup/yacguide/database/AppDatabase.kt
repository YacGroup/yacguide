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

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

import com.yacgroup.yacguide.database.Comment.RegionComment
import com.yacgroup.yacguide.database.Comment.RegionCommentDao
import com.yacgroup.yacguide.database.Comment.RockComment
import com.yacgroup.yacguide.database.Comment.RockCommentDao
import com.yacgroup.yacguide.database.Comment.RouteComment
import com.yacgroup.yacguide.database.Comment.RouteCommentDao
import com.yacgroup.yacguide.database.Comment.SectorComment
import com.yacgroup.yacguide.database.Comment.SectorCommentDao
import com.yacgroup.yacguide.utils.Converters

/*
 * Note: Please do not use this class directly for accessing the database.
 *       DatabaseWrapper provides all necessary methods with much better runtime performance.
 */

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

    companion object {

        private var _instance: AppDatabase? = null

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

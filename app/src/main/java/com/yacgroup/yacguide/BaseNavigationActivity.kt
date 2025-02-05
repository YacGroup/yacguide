/*
 * Copyright (C) 2019 Fabian Kantereit
 *               2023 Axel Paetzold
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

package com.yacgroup.yacguide

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.viewbinding.ViewBinding
import com.google.android.material.navigation.NavigationView

abstract class BaseNavigationActivity<ViewBindingType: ViewBinding> : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    protected lateinit var activityViewBinding: ViewBindingType

    abstract fun getViewBinding(): ViewBindingType

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityViewBinding = getViewBinding()
        setContentView(activityViewBinding.root)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        val navView: NavigationView = findViewById(R.id.nav_view)
        val navMenu: Menu = navView.menu
        navMenu.findItem(R.id.nav_about).title = getString(R.string.menu_about, getString(R.string.app_name))
        val toggle = ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        navView.setNavigationItemSelectedListener(this)

        onBackPressedDispatcher.addCallback(this, object: OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START)
                } else {
                    finish()
                }
            }
        })
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        onStop()
        when (item.itemId) {
            R.id.nav_database -> Intent(this, CountryActivity::class.java)
            R.id.nav_tourbook -> Intent(this, TourbookActivity::class.java)
            R.id.nav_statistics -> Intent(this, StatisticsActivity::class.java)
            R.id.nav_region_manager -> Intent(this, RegionManagerActivity::class.java)
            R.id.nav_preferences -> Intent(this, PreferencesActivity::class.java)
            R.id.nav_about -> Intent(this, AboutActivity::class.java)
            else -> null
        }?.let {
            startActivity(it)
        }
        findViewById<DrawerLayout>(R.id.drawer_layout).closeDrawer(GravityCompat.START)
        finish()
        return true
    }
}

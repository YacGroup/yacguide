package com.example.paetz.yacguide

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View

import com.example.paetz.yacguide.database.AppDatabase
import com.example.paetz.yacguide.utils.IntentConstants
import android.content.pm.PackageManager
import android.widget.TextView

class MainActivity : AppCompatActivity() {
    lateinit var database: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        title = "YACguide"

        val textViewAppVersion: TextView = findViewById(R.id.appVersion)
        try {
            val pkgManager = this.packageManager
            val pkgInfo = pkgManager.getPackageInfo(this.packageName, 0)
            textViewAppVersion.text = pkgInfo.versionName
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }

        database = AppDatabase.getAppDatabase(this)
    }

    @Suppress("UNUSED_PARAMETER")
    fun enterDatabase(v: View) {
        val intent = Intent(this, CountryActivity::class.java)
        startActivity(intent)
    }

    @Suppress("UNUSED_PARAMETER")
    fun enterTourbook(v: View) {
        val intent = Intent(this, TourbookActivity::class.java)
        startActivity(intent)
    }
}

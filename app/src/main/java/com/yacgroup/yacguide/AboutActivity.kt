package com.yacgroup.yacguide

import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.TextView

class AboutActivity : BaseNavigationActivity() {

    override fun getLayoutId(): Int {
        return R.layout.activity_about
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.title = "Über YACguide"

        val textViewAppVersion: TextView = findViewById(R.id.textViewAppVersion)
        textViewAppVersion.text = "Version ${getAppVersion()}"
    }

    private fun getAppVersion() : String {
        try {
            val pkgManager = this.packageManager
            val pkgInfo = pkgManager.getPackageInfo(this.packageName, 0)
            return pkgInfo.versionName
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
        return "0.0"
    }
}

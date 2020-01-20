package com.yacgroup.yacguide

import android.content.pm.PackageManager
import android.os.Bundle
import android.view.Gravity
import android.widget.TextView

class AboutActivity : BaseNavigationActivity() {

    override fun getLayoutId(): Int {
        return R.layout.activity_about
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.title = String.format(getString(R.string.menu_about), getString(R.string.app_name))

        val textViewAppVersion: TextView = findViewById(R.id.textViewAppVersion)
        textViewAppVersion.text = "Version ${getAppVersion()}"
        textViewAppVersion.gravity = Gravity.CENTER_HORIZONTAL
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

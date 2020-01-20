package com.yacgroup.yacguide

import android.app.Dialog
import android.os.Bundle
import android.view.View

import com.yacgroup.yacguide.database.AppDatabase
import com.yacgroup.yacguide.utils.CustomSettings

abstract class TableActivity : BaseNavigationActivity() {
    protected lateinit var db: AppDatabase
    protected lateinit var customSettings: CustomSettings

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        db = AppDatabase.getAppDatabase(this)
        customSettings = CustomSettings.getCustomSettings();
    }

    open fun showComments(v: View) {}

    override fun onResume() {
        super.onResume()

        displayContent()
    }

    protected fun prepareCommentDialog(): Dialog {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.comment_dialog)
        dialog.findViewById<View>(R.id.closeButton).setOnClickListener { dialog.dismiss() }
        dialog.setCanceledOnTouchOutside(false)
        dialog.setCancelable(false)
        dialog.show()

        return dialog
    }

    protected abstract fun displayContent()
}

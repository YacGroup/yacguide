package com.example.paetz.yacguide

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast

import com.example.paetz.yacguide.database.AppDatabase
import com.example.paetz.yacguide.database.Comment.RouteComment
import com.example.paetz.yacguide.database.Route
import com.example.paetz.yacguide.utils.DateUtils
import com.example.paetz.yacguide.utils.IntentConstants
import com.example.paetz.yacguide.utils.WidgetUtils

class DescriptionActivity : TableActivity() {

    private var _route: Route? = null
    private var _resultUpdated: Int = IntentConstants.RESULT_NO_UPDATE

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val routeId = intent.getIntExtra(IntentConstants.ROUTE_KEY, AppDatabase.INVALID_ID)
        super.initialize(R.layout.activity_description)

        _route = db.routeDao().getRoute(routeId)
        val routeStatusId = _route!!.statusId
        if (routeStatusId > 1) {
            (findViewById<View>(R.id.infoTextView) as TextView).text = "Achtung: Der Weg ist ${Route.STATUS[routeStatusId]}"
        }

        displayContent()
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        // Note: Once reset, _resultUpdated may not be set back to RESULT_NO_UPDATE again!
        if (resultCode == IntentConstants.RESULT_UPDATED) {
            _resultUpdated = resultCode
            _route = db.routeDao().getRoute(_route!!.id) // update route instance
            val ascendsButton = findViewById<ImageButton>(R.id.ascendsButton)
            ascendsButton.visibility = if (_route!!.ascendCount > 0) View.VISIBLE else View.INVISIBLE
            Toast.makeText(this, "Begehungen aktualisiert", Toast.LENGTH_SHORT).show()
        }
    }

    override fun back(v: View) {
        val resultIntent = Intent()
        setResult(_resultUpdated, resultIntent)
        finish()
    }

    fun showComments(v: View) {
        val dialog = prepareCommentDialog()

        val layout = dialog.findViewById<LinearLayout>(R.id.commentLayout)
        for (comment in db.routeCommentDao().getAll(_route!!.id)) {
            val qualityId = comment.qualityId
            val gradeId = comment.gradeId
            val securityId = comment.securityId
            val wetnessId = comment.wetnessId
            val text = comment.text

            layout.addView(WidgetUtils.createHorizontalLine(this, 5))
            if (RouteComment.QUALITY_MAP.containsKey(qualityId)) {
                layout.addView(WidgetUtils.createCommonRowLayout(this,
                        "Wegqualität:",
                        RouteComment.QUALITY_MAP[qualityId],
                        WidgetUtils.textFontSizeDp, null,
                        Color.WHITE,
                        Typeface.NORMAL,
                        10, 10, 10, 0))
            }
            if (RouteComment.GRADE_MAP.containsKey(gradeId)) {
                layout.addView(WidgetUtils.createCommonRowLayout(this,
                        "Schwierigkeit:",
                        RouteComment.GRADE_MAP[gradeId],
                        WidgetUtils.textFontSizeDp, null,
                        Color.WHITE,
                        Typeface.NORMAL,
                        10, 10, 10, 0))
            }
            if (RouteComment.SECURITY_MAP.containsKey(securityId)) {
                layout.addView(WidgetUtils.createCommonRowLayout(this,
                        "Absicherung:",
                        RouteComment.SECURITY_MAP[securityId],
                        WidgetUtils.textFontSizeDp, null,
                        Color.WHITE,
                        Typeface.NORMAL,
                        10, 10, 10, 0))
            }
            if (RouteComment.WETNESS_MAP.containsKey(wetnessId)) {
                layout.addView(WidgetUtils.createCommonRowLayout(this,
                        "Abtrocknung:",
                        RouteComment.WETNESS_MAP[wetnessId],
                        WidgetUtils.textFontSizeDp, null,
                        Color.WHITE,
                        Typeface.NORMAL,
                        10, 10, 10, 0))
            }

            layout.addView(WidgetUtils.createCommonRowLayout(this,
                    text,
                    "",
                    WidgetUtils.textFontSizeDp, null,
                    Color.WHITE,
                    Typeface.NORMAL,
                    10, 10, 10, 10))
        }
    }

    fun enterAscend(v: View) {
        val intent = Intent(this@DescriptionActivity, AscendActivity::class.java)
        intent.putExtra(IntentConstants.ROUTE_KEY, _route!!.id)
        startActivityForResult(intent, 0)
    }

    fun goToAscends(v: View) {
        val intent = Intent(this@DescriptionActivity, TourbookAscendActivity::class.java)
        intent.putExtra(IntentConstants.ROUTE_KEY, _route!!.id)
        startActivityForResult(intent, 0)
    }

    override fun displayContent() {
        findViewById<View>(R.id.ascendsButton).visibility = if (_route!!.ascendCount > 0) View.VISIBLE else View.INVISIBLE

        val layout = findViewById<LinearLayout>(R.id.tableLayout)
        layout.removeAllViews()
        this.title = _route!!.name + "   " + _route!!.grade

        var firstAscendClimbers: String = if (_route!!.firstAscendLeader!!.isEmpty())
            "Erstbegeher unbekannt"
        else
            _route!!.firstAscendLeader!!

        firstAscendClimbers += if (_route!!.firstAscendFollower!!.isEmpty())
            ""
        else
            ", " + _route!!.firstAscendFollower!!

        val firstAscendDate = if (_route!!.firstAscendDate == DateUtils.UNKNOWN_DATE)
            "Datum unbekannt"
        else
            DateUtils.formatDate(_route!!.firstAscendDate!!)

        layout.addView(WidgetUtils.createCommonRowLayout(this,
                firstAscendClimbers,
                firstAscendDate,
                WidgetUtils.infoFontSizeDp, null,
                Color.WHITE,
                Typeface.BOLD,
                20, 20, 20, 0))

        val climbingType = _route!!.typeOfClimbing
        if (climbingType!!.isNotEmpty()) {
            layout.addView(WidgetUtils.createCommonRowLayout(this,
                    "Kletterei:",
                    climbingType,
                    WidgetUtils.infoFontSizeDp, null,
                    Color.WHITE,
                    Typeface.NORMAL,
                    20, 20, 20, 0))
        }

        layout.addView(WidgetUtils.createCommonRowLayout(this,
                _route!!.description!!,
                "",
                WidgetUtils.tableFontSizeDp, null,
                Color.WHITE,
                Typeface.BOLD))
    }

    override fun deleteContent() {}
}

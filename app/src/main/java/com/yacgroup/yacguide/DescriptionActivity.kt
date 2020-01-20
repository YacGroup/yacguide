package com.yacgroup.yacguide

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast

import com.yacgroup.yacguide.database.AppDatabase
import com.yacgroup.yacguide.database.Comment.RouteComment
import com.yacgroup.yacguide.database.Route
import com.yacgroup.yacguide.utils.DateUtils
import com.yacgroup.yacguide.utils.IntentConstants
import com.yacgroup.yacguide.utils.WidgetUtils

class DescriptionActivity : TableActivity() {

    private var _route: Route? = null
    private var _resultUpdated: Int = IntentConstants.RESULT_NO_UPDATE

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val routeId = intent.getIntExtra(IntentConstants.ROUTE_KEY, AppDatabase.INVALID_ID)

        _route = db.routeDao().getRoute(routeId)
        val routeStatusId = _route?.statusId ?: 0
        if (routeStatusId > 1) {
            findViewById<TextView>(R.id.infoTextView).text =
                    "Achtung: Der Weg ist ${Route.STATUS[routeStatusId]}"
        }
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        // Note: Once reset, _resultUpdated may not be set back to RESULT_NO_UPDATE again!
        if (resultCode == IntentConstants.RESULT_UPDATED) {
            _resultUpdated = resultCode
            _route = _route?.let { db.routeDao().getRoute(it.id) } // update route instance

            val ascendsButton = findViewById<ImageButton>(R.id.ascendsButton)
            ascendsButton.visibility = if (_route?.ascended() ?: false) View.VISIBLE else View.INVISIBLE
            Toast.makeText(this, getString(R.string.ascends_refreshed), Toast.LENGTH_SHORT).show()
        }
    }

    override fun showComments(v: View) {
        val dialog = prepareCommentDialog()

        val layout = dialog.findViewById<LinearLayout>(R.id.commentLayout)
        val comments = _route?.let { db.routeCommentDao().getAll(it.id) } ?: emptyArray()
        for (comment in comments) {
            val qualityId = comment.qualityId
            val gradeId = comment.gradeId
            val securityId = comment.securityId
            val wetnessId = comment.wetnessId
            val text = comment.text

            layout.addView(WidgetUtils.createHorizontalLine(this, 5))
            if (RouteComment.QUALITY_MAP.containsKey(qualityId)) {
                layout.addView(WidgetUtils.createCommonRowLayout(this,
                        "Wegqualität:",
                        RouteComment.QUALITY_MAP[qualityId].orEmpty(),
                        WidgetUtils.textFontSizeDp,
                        View.OnClickListener { },
                        Color.WHITE,
                        Typeface.NORMAL,
                        10, 10, 10, 0))
            }
            if (RouteComment.GRADE_MAP.containsKey(gradeId)) {
                layout.addView(WidgetUtils.createCommonRowLayout(this,
                        "Schwierigkeit:",
                        RouteComment.GRADE_MAP[gradeId].orEmpty(),
                        WidgetUtils.textFontSizeDp,
                        View.OnClickListener { },
                        Color.WHITE,
                        Typeface.NORMAL,
                        10, 10, 10, 0))
            }
            if (RouteComment.SECURITY_MAP.containsKey(securityId)) {
                layout.addView(WidgetUtils.createCommonRowLayout(this,
                        "Absicherung:",
                        RouteComment.SECURITY_MAP[securityId].orEmpty(),
                        WidgetUtils.textFontSizeDp,
                        View.OnClickListener { },
                        Color.WHITE,
                        Typeface.NORMAL,
                        10, 10, 10, 0))
            }
            if (RouteComment.WETNESS_MAP.containsKey(wetnessId)) {
                layout.addView(WidgetUtils.createCommonRowLayout(this,
                        "Abtrocknung:",
                        RouteComment.WETNESS_MAP[wetnessId].orEmpty(),
                        WidgetUtils.textFontSizeDp,
                        View.OnClickListener { },
                        Color.WHITE,
                        Typeface.NORMAL,
                        10, 10, 10, 0))
            }

            layout.addView(WidgetUtils.createCommonRowLayout(this,
                    text.orEmpty(),
                    "",
                    WidgetUtils.textFontSizeDp,
                    View.OnClickListener { },
                    Color.WHITE,
                    Typeface.NORMAL,
                    10, 10, 10, 10))
        }
    }

    fun enterAscend(v: View) {
        _route?.let {
            val intent = Intent(this@DescriptionActivity, AscendActivity::class.java)
            intent.putExtra(IntentConstants.ROUTE_KEY, it.id)
            startActivityForResult(intent, 0)
        }

    }

    fun goToAscends(v: View) {
        _route?.let {
            val intent = Intent(this@DescriptionActivity, TourbookAscendActivity::class.java)
            intent.putExtra(IntentConstants.ROUTE_KEY, it.id)
            startActivityForResult(intent, 0)
        }
    }

    override fun displayContent() {
        findViewById<View>(R.id.ascendsButton).visibility = if (_route?.ascended() ?: false) View.VISIBLE else View.INVISIBLE

        val layout = findViewById<LinearLayout>(R.id.tableLayout)
        layout.removeAllViews()
        this.title = "${_route?.name.orEmpty()}   ${_route?.grade.orEmpty()}"

        var firstAscendClimbers = _route
                ?.firstAscendLeader
                ?.takeUnless { it.isEmpty() }
                ?: getString(R.string.first_ascend_unknown)

        firstAscendClimbers += _route
                ?.firstAscendFollower
                ?.takeUnless { it.isEmpty() }
                ?.let { ", $it" }
                ?: ""

        val firstAscendDate = _route
                ?.firstAscendDate
                ?.takeUnless { it == DateUtils.UNKNOWN_DATE }
                ?.let { DateUtils.formatDate(it) }
                ?: getString(R.string.date_unknown)


        layout.addView(WidgetUtils.createCommonRowLayout(this,
                firstAscendClimbers,
                firstAscendDate,
                WidgetUtils.infoFontSizeDp,
                View.OnClickListener { },
                Color.WHITE,
                Typeface.BOLD,
                20, 20, 20, 0))

        _route?.typeOfClimbing?.takeIf { it.isNotEmpty() }?.let {
            layout.addView(WidgetUtils.createCommonRowLayout(this,
                    "Kletterei:",
                    it,
                    WidgetUtils.infoFontSizeDp,
                    View.OnClickListener { },
                    Color.WHITE,
                    Typeface.NORMAL,
                    20, 20, 20, 0))
        }

        layout.addView(WidgetUtils.createCommonRowLayout(this,
                _route?.description.orEmpty(),
                "",
                WidgetUtils.tableFontSizeDp,
                View.OnClickListener { },
                Color.WHITE,
                Typeface.BOLD))
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_description
    }
}

package com.example.paetz.yacguide

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast

import com.example.paetz.yacguide.database.AppDatabase
import com.example.paetz.yacguide.database.Comment.RockComment
import com.example.paetz.yacguide.database.Rock
import com.example.paetz.yacguide.utils.AscendStyle
import com.example.paetz.yacguide.utils.IntentConstants
import com.example.paetz.yacguide.utils.WidgetUtils

import java.util.HashSet

class RouteActivity : TableActivity() {

    private var _rock: Rock? = null
    private var _resultUpdated: Int = IntentConstants.RESULT_NO_UPDATE

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        return true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val rockId = intent.getIntExtra(IntentConstants.ROCK_KEY, AppDatabase.INVALID_ID)

        _rock = db.rockDao().getRock(rockId)
        val rockStatus = _rock?.status
        when {
            rockStatus == Rock.statusProhibited -> findViewById<TextView>(R.id.infoTextView).text = "Achtung: Der Felsen ist komplett gesperrt."
            rockStatus == Rock.statusTemporarilyProhibited -> findViewById<TextView>(R.id.infoTextView).text = "Achtung: Der Felsen ist zeitweise gesperrt."
            rockStatus == Rock.statusPartlyProhibited -> findViewById<TextView>(R.id.infoTextView).text = "Achtung: Der Felsen ist teilweise gesperrt."
            _rock?.type == Rock.typeUnofficial -> findViewById<TextView>(R.id.infoTextView).text = "Achtung: Der Felsen ist nicht anerkannt."
        }

        displayContent()
    }

    fun showMap(v: View) {
        val gmmIntentUri = Uri.parse("geo:${_rock?.latitude},${_rock?.longitude}")
        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)

        if (mapIntent.resolveActivity(packageManager) == null) {
            Toast.makeText(this, "Keine Karten-App verfügbar", Toast.LENGTH_SHORT).show()
        } else {
            startActivity(mapIntent)
        }
    }

    override fun showComments(v: View) {
        val dialog = prepareCommentDialog()

        val layout = dialog.findViewById<LinearLayout>(R.id.commentLayout)
        val comments = _rock?.let { db.rockCommentDao().getAll(it.id) } ?: emptyArray()
        for (comment in comments) {
            val qualityId = comment.qualityId
            val text = comment.text

            layout.addView(WidgetUtils.createHorizontalLine(this, 5))
            if (RockComment.QUALITY_MAP.containsKey(qualityId)) {
                layout.addView(WidgetUtils.createCommonRowLayout(this,
                        "Charakter:",
                        RockComment.QUALITY_MAP[qualityId].orEmpty(),
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

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == IntentConstants.RESULT_UPDATED) {
            _resultUpdated = resultCode
            displayContent()
        }
    }

    override fun displayContent() {
        val layout = findViewById<LinearLayout>(R.id.tableLayout)
        layout.removeAllViews()
        this.title = "${_rock?.nr} ${_rock?.name}"

        val routes = _rock?.let { db.routeDao().getAll(it.id) } ?: emptyArray()
        for (route in routes) {
            val commentCount = db.routeCommentDao().getCommentCount(route.id)
            val commentCountAddon = if (commentCount > 0) "   [$commentCount]" else ""

            val onCLickListener = View.OnClickListener {
                val intent = Intent(this@RouteActivity, DescriptionActivity::class.java)
                intent.putExtra(IntentConstants.ROUTE_KEY, route.id)
                startActivityForResult(intent, 0)
            }
            val statusId = route.statusId
            var typeface = Typeface.BOLD
            var bgColor = Color.WHITE
            if (statusId == 3) { // prohibited
                typeface = Typeface.ITALIC
                bgColor = Color.LTGRAY
            }

            val ascends = db.ascendDao().getAscendsForRoute(route.id)
            if (ascends.isNotEmpty()) {
                val rowColors = HashSet<Int>()
                for (ascend in ascends) {
                    AscendStyle.fromId(ascend.styleId)?.let {
                        rowColors.add(it.color)
                    }
                }
                bgColor = AscendStyle.getPreferredColor(rowColors)
            }

            layout.addView(WidgetUtils.createCommonRowLayout(this,
                    "${route.name.orEmpty()}$commentCountAddon",
                    route.grade.orEmpty(),
                    WidgetUtils.tableFontSizeDp,
                    onCLickListener,
                    bgColor,
                    typeface))
            layout.addView(WidgetUtils.createHorizontalLine(this, 1))
        }
    }

    override fun deleteContent() {}

    override fun getLayoutId(): Int {
        return R.layout.activity_route
    }
}


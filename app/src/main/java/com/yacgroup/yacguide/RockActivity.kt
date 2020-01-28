/*
 * Copyright (C) 2018 Axel Pätzold
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

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import com.yacgroup.yacguide.database.AppDatabase

import com.yacgroup.yacguide.database.Comment.SectorComment
import com.yacgroup.yacguide.database.Rock
import com.yacgroup.yacguide.database.Sector
import com.yacgroup.yacguide.network.RockParser
import com.yacgroup.yacguide.utils.IntentConstants
import com.yacgroup.yacguide.utils.WidgetUtils

class RockActivity : UpdatableTableActivity() {

    private var _sector: Sector? = null
    private var _onlySummits: Boolean = false
    private var _rockNamePart: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sectorId = intent.getIntExtra(IntentConstants.SECTOR_KEY, AppDatabase.INVALID_ID)

        jsonParser = RockParser(db, this, sectorId)
        _sector = db.sectorDao().getSector(sectorId)

        val searchEditText = findViewById<EditText>(R.id.searchEditText)
        searchEditText.onFocusChangeListener = View.OnFocusChangeListener { view, _ ->
            val imm = view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable) {
                _rockNamePart = searchEditText.text.toString()
                displayContent()
            }
        })
    }

    override fun showComments(v: View) {
        val dialog = prepareCommentDialog()

        val layout = dialog.findViewById<LinearLayout>(R.id.commentLayout)
        val comments = _sector?.let { db.sectorCommentDao().getAll(it.id) } ?: emptyArray()
        for (comment in comments) {
            val qualityId = comment.qualityId
            val text = comment.text

            layout.addView(WidgetUtils.createHorizontalLine(this, 5))
            if (SectorComment.QUALITY_MAP.containsKey(qualityId)) {
                layout.addView(WidgetUtils.createCommonRowLayout(this,
                        "Bedeutung:",
                        SectorComment.QUALITY_MAP[qualityId].orEmpty(),
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

    fun onlySummitsCheck(v: View) {
        _onlySummits = findViewById<CheckBox>(R.id.onlySummitsCheckBox).isChecked
        displayContent()
    }

    override fun displayContent() {
        val layout = findViewById<LinearLayout>(R.id.tableLayout)
        layout.removeAllViews()
        this.title = _sector?.name.orEmpty()
        val rocks = _sector?.let { db.rockDao().getAll(it.id) } ?: emptyArray()
        for (rock in rocks) {
            val rockName = rock.name.orEmpty()
            if (!rockName.toLowerCase().contains(_rockNamePart.toLowerCase())) {
                continue
            }
            val type = rock.type
            val status = rock.status
            if (_onlySummits && !rockIsAnOfficialSummit(rock)) {
                continue
            }
            var bgColor = Color.WHITE
            var typeface = Typeface.BOLD
            var typeAdd = ""
            if (type != Rock.typeSummit) {
                typeface = Typeface.NORMAL
                typeAdd = "  ($type)"
            }
            val botchAdd = if (rock.ascendCountBotch > 0) getString(R.string.botch) else ""
            val projectAdd = if (rock.ascendCountProject > 0) getString(R.string.project) else ""
            val watchingAdd = if (rock.ascendCountWatching > 0) getString(R.string.watching) else ""
            if (status == Rock.statusProhibited) {
                typeface = Typeface.ITALIC
                bgColor = Color.LTGRAY
            }
            val onClickListener = View.OnClickListener {
                val intent = Intent(this@RockActivity, RouteActivity::class.java)
                intent.putExtra(IntentConstants.ROCK_KEY, rock.id)
                startActivityForResult(intent, 0)
            }
            layout.addView(WidgetUtils.createCommonRowLayout(this,
                    "${rock.nr}  $rockName$typeAdd$botchAdd$projectAdd$watchingAdd",
                    status.toString(),
                    WidgetUtils.tableFontSizeDp,
                    onClickListener,
                    if (rock.ascendCountLead > 0) customSettings.getColorLead()
                        else if (rock.ascendCountFollow > 0) customSettings.getColorFollow()
                            else bgColor,
                    typeface))
            layout.addView(WidgetUtils.createHorizontalLine(this, 1))
        }
    }

    override fun deleteContent() {
        _sector?.let {
            db.deleteRocks(it.id)
        }
    }

    private fun rockIsAnOfficialSummit(rock: Rock): Boolean {
        return (rock.type == Rock.typeSummit || rock.type == Rock.typeAlpine) && rock.status != Rock.statusProhibited
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_rock
    }
}

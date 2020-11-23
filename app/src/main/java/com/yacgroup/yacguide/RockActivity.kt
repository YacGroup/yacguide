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

import com.yacgroup.yacguide.database.Comment.SectorComment
import com.yacgroup.yacguide.database.DatabaseWrapper
import com.yacgroup.yacguide.database.Rock
import com.yacgroup.yacguide.database.Sector
import com.yacgroup.yacguide.utils.AscendStyle
import com.yacgroup.yacguide.utils.IntentConstants
import com.yacgroup.yacguide.utils.ParserUtils
import com.yacgroup.yacguide.utils.WidgetUtils

class RockActivity : TableActivity() {

    private var _sector: Sector? = null
    private var _onlySummits: Boolean = false
    private var _onlyProjects: Boolean = false
    private var _rockNamePart: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sectorId = intent.getIntExtra(IntentConstants.SECTOR_KEY, DatabaseWrapper.INVALID_ID)

        _sector = db.getSector(sectorId)

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

        _onlySummits = customSettings.getBoolean(
                getString(R.string.display_only_official_summits),
                resources.getBoolean(R.bool.display_only_official_summits))
        findViewById<CheckBox>(R.id.onlySummitsCheckBox).isChecked = _onlySummits
    }

    override fun onStop() {
        val editor = customSettings.edit()
        editor.putBoolean(getString(R.string.display_only_official_summits), findViewById<CheckBox>(R.id.onlySummitsCheckBox).isChecked)
        editor.commit()
        super.onStop()
    }

    override fun showComments(v: View) {
        val dialog = prepareCommentDialog()

        val layout = dialog.findViewById<LinearLayout>(R.id.commentLayout)
        val comments = _sector?.let { db.getSectorComments(it.id) } ?: emptyList()
        for (comment in comments) {
            val qualityId = comment.qualityId
            val text = comment.text

            layout.addView(WidgetUtils.createHorizontalLine(this, 5))
            if (SectorComment.QUALITY_MAP.containsKey(qualityId)) {
                layout.addView(WidgetUtils.createCommonRowLayout(this,
                        textLeft = getString(R.string.relevance),
                        textRight = SectorComment.QUALITY_MAP[qualityId].orEmpty(),
                        textSizeDp = WidgetUtils.textFontSizeDp,
                        typeface = Typeface.NORMAL))
            }
            layout.addView(WidgetUtils.createCommonRowLayout(this,
                    textLeft = text.orEmpty(),
                    textSizeDp = WidgetUtils.textFontSizeDp,
                    typeface = Typeface.NORMAL))
        }
    }

    fun onlySummitsCheck(v: View) {
        _onlySummits = findViewById<CheckBox>(R.id.onlySummitsCheckBox).isChecked
        displayContent()
    }

    fun onlyProjectsCheck(v: View) {
        _onlyProjects = findViewById<CheckBox>(R.id.onlyProjectsCheckbox).isChecked
        displayContent()
    }

    override fun displayContent() {
        val layout = findViewById<LinearLayout>(R.id.tableLayout)
        layout.removeAllViews()
        val sectorName = ParserUtils.decodeObjectNames(_sector?.name)
        this.title = if (sectorName.first.isNotEmpty()) sectorName.first else sectorName.second
        val requestedStyleId = if (_onlyProjects) AscendStyle.ePROJECT.id else null
        val rocks = _sector?.let { db.getRocksForSector(it.id, requestedStyleId) } ?: emptyList()
        for (rock in rocks) {
            val rockName = ParserUtils.decodeObjectNames(rock.name)
            if (_rockNamePart.isNotEmpty() && rockName.toList().none{ it.toLowerCase().contains(_rockNamePart.toLowerCase()) }) {
                continue
            }
            if (_onlySummits && !_rockIsAnOfficialSummit(rock)) {
                continue
            }
            var bgColor = Color.WHITE
            var typeface = Typeface.BOLD
            var typeAdd = ""
            if (rock.type != Rock.typeSummit) {
                typeface = Typeface.NORMAL
                typeAdd = "  (${rock.type})"
            }
            if (rock.status == Rock.statusProhibited || rock.status == Rock.statusCollapsed) {
                typeface = Typeface.ITALIC
                bgColor = Color.LTGRAY
            }
            bgColor = colorizeEntry(rock.ascendsBitMask, bgColor)
            val decorationAdd = decorateEntry(rock.ascendsBitMask)
            val onClickListener = View.OnClickListener {
                val intent = Intent(this@RockActivity, RouteActivity::class.java)
                intent.putExtra(IntentConstants.ROCK_KEY, rock.id)
                startActivityForResult(intent, 0)
            }
            layout.addView(WidgetUtils.createCommonRowLayout(this,
                    textLeft = "${rock.nr}  ${rockName.first}$typeAdd$decorationAdd",
                    textRight = rock.status.toString(),
                    onClickListener = onClickListener,
                    bgColor = bgColor,
                    typeface = typeface))
            layout.addView(WidgetUtils.createCommonRowLayout(this,
                    textLeft = rockName.second,
                    textSizeDp = WidgetUtils.textFontSizeDp,
                    onClickListener = onClickListener,
                    bgColor = bgColor,
                    typeface = typeface))
            layout.addView(WidgetUtils.createHorizontalLine(this, 1))
        }
    }

    private fun _rockIsAnOfficialSummit(rock: Rock): Boolean {
        return (rock.type == Rock.typeSummit || rock.type == Rock.typeAlpine)
                && rock.status != Rock.statusProhibited
                && rock.status != Rock.statusCollapsed
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_rock
    }
}

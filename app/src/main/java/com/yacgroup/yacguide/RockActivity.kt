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
import android.widget.*

import com.yacgroup.yacguide.database.comment.SectorComment
import com.yacgroup.yacguide.database.DatabaseWrapper
import com.yacgroup.yacguide.database.Rock
import com.yacgroup.yacguide.database.Sector
import com.yacgroup.yacguide.utils.IntentConstants
import com.yacgroup.yacguide.utils.ParserUtils
import com.yacgroup.yacguide.utils.WidgetUtils

class RockActivity : TableActivity(), AdapterView.OnItemSelectedListener {

    private lateinit var _sector: Sector
    private var _rockFilter = ElementFilter.eNone;
    private var _rockNamePart: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sectorId = intent.getIntExtra(IntentConstants.SECTOR_KEY, DatabaseWrapper.INVALID_ID)

        _sector = db.getSector(sectorId)!!

        val filterSpinner: Spinner = findViewById(R.id.filterSpinner)
        ArrayAdapter.createFromResource(
                this,
                R.array.rockFilters,
                android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            filterSpinner.adapter = adapter
            filterSpinner.onItemSelectedListener = this
        }

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
        val comments = db.getSectorComments(_sector.id)
        if (comments.isNotEmpty()) {
            prepareCommentDialog().findViewById<LinearLayout>(R.id.commentLayout)?.let {
                for ((idx, comment) in comments.withIndex()) {
                    val qualityId = comment.qualityId
                    val text = comment.text

                    if (idx > 0) {
                        it.addView(WidgetUtils.createHorizontalLine(this, 1))
                    }
                    if (SectorComment.QUALITY_MAP.containsKey(qualityId)) {
                        it.addView(WidgetUtils.createCommonRowLayout(this,
                                textLeft = getString(R.string.relevance),
                                textRight = SectorComment.QUALITY_MAP[qualityId].orEmpty(),
                                textSizeDp = WidgetUtils.textFontSizeDp,
                                typeface = Typeface.NORMAL))
                    }
                    it.addView(WidgetUtils.createCommonRowLayout(this,
                            textLeft = text.orEmpty(),
                            textSizeDp = WidgetUtils.textFontSizeDp,
                            typeface = Typeface.NORMAL))
                }
            }
        } else {
            showNoCommentToast()
        }
    }

    override fun displayContent() {
        val layout = findViewById<LinearLayout>(R.id.tableLayout)
        layout.removeAllViews()
        val sectorName = ParserUtils.decodeObjectNames(_sector.name)
        this.title = if (sectorName.first.isNotEmpty()) sectorName.first else sectorName.second
        val rocks = when (_rockFilter) {
            ElementFilter.eOfficial -> db.getRocksForSector(_sector.id).filter { _rockIsAnOfficialSummit(it) }
            ElementFilter.eProject -> db.getProjectedRocksForSector(_sector.id)
            else -> db.getRocksForSector(_sector.id)
        }
        for (rock in rocks) {
            val rockName = ParserUtils.decodeObjectNames(rock.name)
            if (_rockNamePart.isNotEmpty() && rockName.toList().none{ it.toLowerCase().contains(_rockNamePart.toLowerCase()) }) {
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

    // AdapterView.OnItemSelectedListener
    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        _rockFilter = filterMap[position] ?: ElementFilter.eNone
        displayContent()
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {}
}

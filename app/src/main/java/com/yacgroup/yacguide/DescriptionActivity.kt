/*
 * Copyright (C) 2019, 2022 Axel Paetzold
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

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.yacgroup.yacguide.database.Route
import com.yacgroup.yacguide.list_adapters.AscendViewAdapter
import com.yacgroup.yacguide.utils.*

class DescriptionActivity : TableActivity() {

    private lateinit var _viewAdapter: AscendViewAdapter
    private lateinit var _route: Route

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _route = db.getRoute(activityLevel.parentId)!!
        if (_route.statusId > 1) {
            findViewById<TextView>(R.id.infoTextView).text =
                "${getString(R.string.route_restricted)} ${Route.STATUS[_route.statusId]}"
        }

        _viewAdapter = AscendViewAdapter(this, customSettings) {
            ascendId -> _onAscendSelected(ascendId)
        }
        findViewById<RecyclerView>(R.id.tableRecyclerView).adapter = _viewAdapter
    }

    override fun getLayoutId() = R.layout.activity_description

    override fun onResume() {
        super.onResume()
        displayContent()
    }

    override fun showComments(v: View) {
        showRouteComments(activityLevel.parentId)
    }

    @Suppress("UNUSED_PARAMETER")
    fun enterAscend(v: View) {
        startActivity(Intent(this@DescriptionActivity, AscendActivity::class.java).apply {
            putExtra(IntentConstants.CLIMBING_OBJECT_PARENT_ID, _route.id)
        })
    }

    override fun displayContent() {
        val routeName = ParserUtils.decodeObjectNames(_route.name)
        this.title = "${if (routeName.first.isNotEmpty()) routeName.first else routeName.second}   ${_route.grade.orEmpty()}"

        var firstAscendClimbers = _route.firstAscendLeader
                ?.takeUnless { it.isEmpty() }
                ?: getString(R.string.first_ascend_unknown)
        firstAscendClimbers += _route.firstAscendFollower
                ?.takeUnless { it.isEmpty() }
                ?.let { ", $it" }
                ?: ""
        val firstAscendDate = _route.firstAscendDate
                ?.takeUnless { it == DateUtils.UNKNOWN_DATE }
                ?.let { DateUtils.formatDate(it) }
                ?: getString(R.string.date_unknown)

        findViewById<TextView>(R.id.firstAscendClimbersTextView).text = firstAscendClimbers
        findViewById<TextView>(R.id.firstAscendDateTextView).text = firstAscendDate

        _route.typeOfClimbing?.takeIf { it.isNotEmpty() }?.let {
            findViewById<ConstraintLayout>(R.id.typeOfClimbingLayout).visibility = View.VISIBLE
            findViewById<TextView>(R.id.typeOfClimbingTextView).text = it
        }

        findViewById<TextView>(R.id.routeDescriptionTextView).text = _route.description

        val ascends = db.getRouteAscends(_route.id)
        findViewById<TextView>(R.id.myAscendsTextView).visibility =
            if (ascends.isEmpty()) View.GONE
            else View.VISIBLE
        _viewAdapter.submitList(ascends)
    }

    private fun _onAscendSelected(ascendId: Int) {
        startActivity(Intent(this@DescriptionActivity, TourbookAscendActivity::class.java).apply {
            putExtra(IntentConstants.ASCEND_ID, ascendId)
        })
    }
}

/*
 * Copyright (C) 2022 Axel Paetzold
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

package com.yacgroup.yacguide.list_adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.yacgroup.yacguide.R
import com.yacgroup.yacguide.database.Country

class CountryViewAdapter(private val _onClick: (String) -> Unit)
    : ListAdapter<Country, RecyclerView.ViewHolder>(CountryDiffCallback) {

    inner class CountryViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val _listItemLayout = view.findViewById<LinearLayout>(R.id.listItemLayout)
        private val _mainLeftTextView = view.findViewById<TextView>(R.id.mainLeftTextView)

        fun bind(country: Country) {
            _mainLeftTextView.text = country.name
            _listItemLayout.setOnClickListener {
                _onClick(country.name)
            }
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(viewGroup.context)
        val view = inflater.inflate(R.layout.list_data_item, viewGroup, false)
        return CountryViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
        (viewHolder as CountryViewHolder).bind(getItem(position) as Country)
    }
}

object CountryDiffCallback : DiffUtil.ItemCallback<Country>() {
    override fun areItemsTheSame(oldCountry: Country, newCountry: Country): Boolean {
        return oldCountry.name == newCountry.name
    }

    override fun areContentsTheSame(oldCountry: Country, newCountry: Country): Boolean {
        return oldCountry == newCountry
    }
}
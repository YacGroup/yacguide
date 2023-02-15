/*
 * Copyright (C) 2023 Axel Paetzold
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

import androidx.recyclerview.widget.DiffUtil
import com.yacgroup.yacguide.database.Ascend

object AscendDiffCallback : DiffUtil.ItemCallback<Ascend>() {
    override fun areItemsTheSame(oldAscend: Ascend, newAscend: Ascend): Boolean {
        return oldAscend.id == newAscend.id
    }

    override fun areContentsTheSame(oldAscend: Ascend, newAscend: Ascend): Boolean {
        return oldAscend == newAscend
    }
}
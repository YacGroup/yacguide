/*
 * Copyright (C) 2020 Christian Sommer
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

package com.yacgroup.yacguide.markwon

import io.noties.markwon.AbstractMarkwonPlugin
import io.noties.markwon.core.MarkwonTheme

open class BaseMarkwonPlugin: AbstractMarkwonPlugin() {
    override fun configureTheme(builder: MarkwonTheme.Builder) {
        // Set default heading break height to 0.
        builder.headingBreakHeight(0)
        // Scale down headings
        builder.headingTextSizeMultipliers(floatArrayOf(1.4f, 1.2f, 1.1f, 1f, .9f, .8f))
    }
}

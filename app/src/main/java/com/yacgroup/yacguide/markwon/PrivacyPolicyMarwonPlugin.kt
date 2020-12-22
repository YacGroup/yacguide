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

class PrivacyPolicyMarkwonPlugin(private val _heading: String): BaseMarkwonPlugin() {

    override fun processMarkdown(markdown: String): String {
        // Replace front matter from markdown file by markdown section.
        val regex =  Regex("^---.*---$",
                setOf(RegexOption.MULTILINE, RegexOption.DOT_MATCHES_ALL))
        return markdown.replace(regex, "# ".plus(_heading))
    }
}

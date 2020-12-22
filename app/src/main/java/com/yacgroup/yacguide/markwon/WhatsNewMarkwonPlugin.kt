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

class WhatsNewMarkwonPlugin(private val _githubUrl: String): BaseMarkwonPlugin() {

    override fun processMarkdown(markdown: String): String {
        // Add link to GitHub issue, referenced in the markdown file with #<issueId>.
        val regexSearch = Regex("""#(\d+)""")
        val issueUrl = _githubUrl + "/issues/"
        val regexReplace = "[#$1](" + issueUrl + "$1)"
        return markdown.replace(regexSearch, regexReplace)
    }
}

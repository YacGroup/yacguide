# YacGuide Android Application
#
# Copyright (C) 2026 Christian Sommer
#
# This program is free software: you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation, either version 3 of the License, or
# (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with this program.  If not, see <https://www.gnu.org/licenses/>.

"""Test coverage related stuff"""

import xml.etree.ElementTree as ET

from . import utils


# Marker to find the PR comment again for updating it
COMMENT_MARKER = "<!-- full-coverage-report -->"

COUNTER_TYPES = ["INSTRUCTION", "BRANCH", "LINE", "METHOD", "CLASS"]


def get_counters(element):
    """Return the coverage counters of a report element.

    Only the direct children are considered because they contain the
    accumulated values of the element.

    Args:
        element (xml.etree.ElementTree.Element): Report or package element

    Returns:
        dict: Counter type mapped to the tuple (covered, total)
    """
    counters = {}
    for counter in element.findall("counter"):
        covered = int(counter.get("covered"))
        missed = int(counter.get("missed"))
        counters[counter.get("type")] = (covered, covered + missed)
    return counters


def format_counter(counters, counter_type):
    """Return a counter formatted as table cell.

    Args:
        counters (dict): Counters as returned by `get_counters`
        counter_type (str): Counter type

    Returns:
        str: Formatted counter
    """
    covered, total = counters.get(counter_type, (0, 0))
    if total == 0:
        return "-"
    return f"{100 * covered / total:.1f}% ({covered}/{total})"


def create_summary(report_file, title):
    """Create a Markdown summary of the full test coverage.

    Args:
        report_file (str): JaCoCo XML report file
        title (str): Title of the summary

    Returns:
        str: Markdown summary
    """
    utils.debug("Reading JaCoCo report '%s'." % report_file)
    report = ET.parse(report_file).getroot()
    header = "|" + "|".join(t.capitalize() for t in COUNTER_TYPES) + "|"
    separator = "|" + "|".join(":-" for _ in COUNTER_TYPES) + "|"

    total_counters = get_counters(report)
    lines = [
        COMMENT_MARKER,
        f"### {title}",
        "",
        header,
        separator,
        "|" + "|".join(format_counter(total_counters, t)
                       for t in COUNTER_TYPES) + "|",
        "",
        "<details>",
        "<summary>Coverage per package</summary>",
        "",
        "|Package" + header,
        "|:-" + separator,
    ]
    packages = sorted(report.findall("package"), key=lambda p: p.get("name"))
    for package in packages:
        counters = get_counters(package)
        name = package.get("name").replace("/", ".")
        lines.append(f"|`{name}`|"
                     + "|".join(format_counter(counters, t)
                                for t in COUNTER_TYPES) + "|")
    lines += ["", "</details>", ""]
    return "\n".join(lines)


def write_summary(report_file, output_file, title):
    """Write a Markdown summary of the full test coverage to a file.

    Args:
        report_file (str): JaCoCo XML report file
        output_file (str): Markdown output file
        title (str): Title of the summary
    """
    summary = create_summary(report_file, title)
    with open(output_file, "w", encoding="utf-8") as file:
        file.write(summary)
    utils.info("Wrote coverage summary to '%s'." % output_file)

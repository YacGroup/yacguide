# YacGuide Android Application
#
# Copyright (C) 2020 Christian Sommer
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

"""This code is executed, if the package is loaded."""

import logging


def init_logging(debug=False):
    """Initialize logging module.

    Args:
        level (init): Initial logging level
    """
    if debug:
        level = logging.DEBUG
    else:
        level = logging.INFO
    logging.basicConfig(format="%(levelname)s: %(message)s",
                        level=level)

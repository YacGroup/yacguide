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

"""General utility functions"""

import sys
import os
import shutil
import subprocess
import logging


def debug(msg):
    """Print debug message.

    Args:
        msg (str): Message string
    """
    logging.debug(msg)


def error(msg):
    """Print error message and exit with code -1.

    Args:
        msg (str): Message string
    """
    logging.error(msg)
    sys.exit(-1)


def info(msg):
    """Print info message.

    Args:
        msg (str): Message string
    """
    logging.info(msg)


def run_cmd(cmd):
    """Run shell command."""
    debug("Running shell command '%s'." % cmd)
    subprocess.run(cmd, shell=True, check=True)


def clean(distclean=False):
    """Run clean-up.

    Args:
        distclean (bool): If True, clean everything.
    """
    cmd = "git clean -Xfd"
    run_cmd(cmd)
    if distclean:
        debug("Running distclean.")
        shutil.rmtree(os.path.join("app", "build"),
                      ignore_errors=True)

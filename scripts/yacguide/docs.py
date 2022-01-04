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

"""Utilities for building documentation"""

from . import docker


class Docs():
    """Class for building documentation based on Jekyll."""

    def __init__(self):
        self.container = docker.BuildContainer()

    def prepare_jekyll(self):
        """Setup Jekyll inside the container"""
        cmds = ["cd docs"]
        cmds += ["bundle config set --local path 'vendor/bundle'"]
        cmd_line = " && ".join(cmds)
        self.container.execute_user(cmd_line)

    def jekyll_serve(self):
        """Run Jekyll web server."""
        cmds = ["cd docs"]
        addr = self.container.get_ip_address()
        cmds += ["bundle exec jekyll serve --host %s" % addr]
        cmd_line = " && ".join(cmds)
        self.container.execute_user(cmd_line)

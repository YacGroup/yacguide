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

"""App related stuff"""

import os
import re
from datetime import datetime
import shlex
# https://gitpython.readthedocs.io/en/stable/index.html
import git
import packaging.version

from . import utils
from . import docker


RELEASE_TYPES = ["dev", "stable"]
GRADLE_FILE = os.path.join(os.getcwd(), "app", "build.gradle")


class App():
    """App class"""

    def __init__(self):
        self.container = docker.BuildContainer()

    def exec_gradle_cmd(self, args):
        """Execute Gradle command with given arguments.

        Args:
            args (str, list(str)): Gradle command arguments
        """
        cmd = ["./gradlew"]
        cmd += ["--gradle-user-home"]
        cmd += [os.path.join("$(pwd)", ".gradle")]
        if isinstance(args, list):
            cmd += args
        else:
            cmd += [args]
        cmd_str = shlex.join(cmd)
        self.container.execute_user(cmd_str)

    def run_tests(self):
        """Run tests."""
        self.exec_gradle_cmd("test")

    def build_dists(self, releases=None):
        """Build distributions.

        Args:
            releases (list(str), optional): Releases to build.
                If not specified, all release are built.
        """
        targets = ["clean"]
        if releases:
            for release in releases:
                target = "bundle" + release.capitalize() + "Release"
                targets.append(target)
        self.exec_gradle_cmd(targets)

    def deploy(self, release=None):
        """Deploy app to Google Play Store.

        Args:
            release (str, optional): Releases to deploy.
        """
        target = "publish{0}ReleaseBundle".\
            format(release.capitalize())
        self.exec_gradle_cmd(target)


class Release():
    """Release Class

    Attributes:
        repo (git.Repo): Git repo object
        skip_checks (bool): Skip checks
    """
    def __init__(self, skip_checks=False):
        self.repo = git.Repo(os.getcwd())
        self.skip_checks = skip_checks

    def pre_checks(self):
        """Run some checks before creating the release."""
        if not self.skip_checks:
            if self.repo.is_dirty():
                utils.error("Repo is dirty.")

    @classmethod
    def set_version_code(cls, gradle_str, flavor, code):
        """Set version code inside the given Gradle file string.

        Args:
            gradle_str (str): Content of Gradle file to be updated.
            flavor (str): App flavor to be considered.
            code (str): Version to be set.

        Returns:
           str: Update Gradle string.
        """
        rexpr = re.compile(
            r'(%s\s*\{[^\}]*versionCode)\s+[\d\.]+' % flavor,
            re.MULTILINE)
        gradle_str_updated = rexpr.sub(
            r'\1 %s' % code,
            gradle_str)
        return gradle_str_updated

    @classmethod
    def set_version_name(cls, gradle_str, flavor, name):
        """Set version name inside given Gradle file string.

        Args:
            gradle_str (str): Content of Gradle file to be updated.
            flavor (str): App flavor to be considered
            name (str): Wersion to be set

        Returns:
            str: Updated Gradle string.
        """
        rexpr = re.compile(
            rf"""
            ({flavor}\s*\{{[^\}}]*versionName)\s+('|")[\d\.]+('|")
            """,
            re.MULTILINE | re.VERBOSE)
        gradle_str_updated = rexpr.sub(
            rf"\g<1> \g<2>{name}\g<3>",
            gradle_str)
        return gradle_str_updated

    @classmethod
    def version_to_code(cls, version, flavor="stable"):
        """Convert version into corresponding code.

        Args:
            version (str): Version string
            flavor (str, optional): Android flavor

        Returns:
            int: Version code
        """
        if flavor == "stable":
            # Convert 'x.y.z' to 'XXXYYYZZZ'
            major, minor, bug = version.split(".")
            code_str = "{0:0>3}{1:0>3}{2:0>3}".format(
                major, minor, bug
            )
            # Get rid of leading zeros.
            code = int(code_str)
        else:
            raise NotImplementedError
        return code

    def update_dev_version(self, version):
        """Update development version code and name in Gradle file.

        Args:
            version (str): Version code
        """
        with open(GRADLE_FILE) as fobj:
            build_gradle = fobj.read()
        build_gradle = self.set_version_code(
            gradle_str=build_gradle,
            flavor="dev",
            code=version
        )
        build_gradle = self.set_version_name(
            gradle_str=build_gradle,
            flavor="dev",
            name=version
        )
        with open(GRADLE_FILE, "w") as fobj:
            fobj.write(build_gradle)
        self.repo.index.add([GRADLE_FILE])
        self.repo.index.commit("Daily dev %s" % version)

    def create_dev_release(self, version=None):
        """Create a development release for given version.

        Args:
            version (str, optional): Version identifier
        """
        if not version:
            version = datetime.now().strftime("%Y%m%d")
        tag_name = "dev-%s" % version
        self.update_dev_version(version)
        self.repo.create_tag(
            path=tag_name,
            message="Daily dev %s" % version)
        print("""\n
Next step:
----------
  Push the release commit and tag using 'git push --follow-tags'.
""")

    def update_stable_version(self, version):
        """Update stable version code and name in Gradle file."""
        with open(GRADLE_FILE) as fobj:
            build_gradle = fobj.read()
        build_gradle = self.set_version_code(
            gradle_str=build_gradle,
            flavor="stable",
            code=self.version_to_code(version)
        )
        build_gradle = self.set_version_name(
            gradle_str=build_gradle,
            flavor="stable",
            name=version
        )
        with open(GRADLE_FILE, "w") as fobj:
            fobj.write(build_gradle)
        self.repo.index.add([GRADLE_FILE])
        self.repo.index.commit("Release %s" % version)

    def check_stable_release(self, version):
        """Check version string for stable release."""
        # Version not specified?
        if not version:
            utils.error("Empty version.")
        # Format correct?
        rexpr = re.compile(r'\d+\.\d+.\d+')
        if not rexpr.match(version):
            utils.error("Invalid version format.")
        # Release tag already exists?
        try:
            self.repo.tags["v" + version]
        except IndexError:
            # Tag does not exists.
            pass
        else:
            utils.error("Version '%s' already exists." % version)
        # Version is increasing?
        pkg_ver_parse = packaging.version.parse
        for tag in self.repo.tags:
            tag_version = tag.name[1:]
            if pkg_ver_parse(version) < pkg_ver_parse(tag_version):
                utils.error("Version not increasing.")

    def create_stable_release(self, version):
        """Create a stable release.

        Args:
            version (str): Version to be created
        """
        if not self.skip_checks:
            self.check_stable_release(version=version)
        self.update_stable_version(version)
        tag_name = "v%s" % version
        self.repo.create_tag(
            path=tag_name,
            message="Release %s" % version)
        print("""\n
Next step:
----------
  Push the release tag using 'git push --follow-tags'.
""")

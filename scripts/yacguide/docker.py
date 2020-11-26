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

"""Docker container stuff"""

import os
import grp
import pwd
import configparser
# https://docker-py.readthedocs.io/en/stable/index.html
import docker

from . import utils

IMG_VER = "20201005"
IMG = "yacgroup/yacguide-build:" + IMG_VER
CONTAINER_NAME = "yacguide-build"
NETWORK_NAME = "yacnet"
MOUNT_TARGET = "/mnt/yacguide-build"


class BuildContainer():
    """Build Container Class"""

    def __init__(self):
        self.client = docker.from_env()

    @classmethod
    def _get_user_infos(cls):
        """Return user information.

        Returns:
            dict: User information as dictionary.
        """
        uid = os.getuid()
        pwdb = pwd.getpwuid(uid)
        gid = pwdb.pw_gid
        grpdb = grp.getgrgid(gid)
        user_infos = {
            "uid": uid,
            "gid": gid,
            "user": pwdb.pw_name,
            "group": grpdb.gr_name,
            "home": pwdb.pw_dir,
        }
        return user_infos

    def _prepare_user(self):
        """Create same user including the primary group inside the
        container."""
        user_infos = self._get_user_infos()
        group_add_cmd = "groupadd --gid {gid} {group}".format(
            **user_infos)
        self.execute_root_shell(group_add_cmd)
        user_add_cmd = ("useradd --create-home --uid {uid}"
                        " --gid {gid} {user}").format(**user_infos)
        self.execute_root_shell(user_add_cmd)

    def _prepare_git(self):
        """Copy some Git configurations from the user into the
        container."""
        git_config = configparser.ConfigParser()
        user_info = self._get_user_infos()
        git_config_file = os.path.join(user_info["home"],
                                       ".gitconfig")
        git_config.read(git_config_file)
        git_user_name = git_config["user"]["name"]
        git_user_email = git_config["user"]["email"]
        cmds = ['git config --global user.email "{email}"']
        cmds += ['git config --global user.name "{name}"']
        for cmd in cmds:
            cmd_fmt = cmd.format(email=git_user_email,
                                 name=git_user_name)
            self.execute_user(cmd_fmt)

    def _execute(self, cmd, **add_exec_args):
        """Execute shell command inside a running container.

        Args:
            cmd (str): Command string to be executed.
            add_exec_args: Additional arguments for
                container.exec_run()
        """
        container = self.start()
        exec_args = {
            "stream": True,
            "cmd": "/bin/bash -c '%s'" % cmd
        }
        exec_args.update(add_exec_args)
        utils.info("Executing command inside container: %s" %
                   exec_args["cmd"])
        _exit_code, output = container.exec_run(**exec_args)
        for chunk in output:
            print(chunk.decode(), end="")

    def _start_new(self):
        """Start new container."""
        mount = docker.types.Mount(
            target=MOUNT_TARGET,
            source=os.getcwd(),
            type="bind")
        net = self.get_network()
        if net:
            net.remove()
        net = self.client.networks.create(
            name=NETWORK_NAME, driver="bridge",
        )
        run_options = {
            "image": IMG,
            "name": CONTAINER_NAME,
            "network": NETWORK_NAME,
            "ports": {"4000/tcp": 4000},
            "detach": True,
            "mounts": [mount],
            "command": "/bin/bash -c 'tail -f /dev/null'",
        }
        utils.info("Starting new container ...")
        self.client.containers.run(**run_options)
        self.prepare()

    def start(self, start_new=False):
        """Start a container.

        Args:
            start_new (bool): Start new container. Stops and removes
                existing container.

        Returns:
            docker.models.containers.Container: Started container
        """
        if start_new:
            self.stop()
            self.remove()
            self._start_new()
        else:
            container = self.get_container()
            if container:
                if container.status == "running":
                    utils.debug("Container already running.")
                elif container.status == "exited":
                    utils.debug("Starting existing container ...")
                    container.start()
            else:
                self._start_new()
        return self.get_container()

    def stop(self):
        """Stop container, if exists."""
        container = self.get_container()
        if container:
            utils.info("Stopping container ...")
            container.stop(timeout=0)

    def remove(self):
        """Remove container if exists."""
        container = self.get_container()
        if container:
            if container.status == "running":
                self.stop()
            utils.info("Removing container ...")
            container.remove()

    def get_container(self):
        """Return existing build container regardless of the
        status.

        Returns:
            docker.models.containers.Container: Container found or
                None, if no such container exists.
        """
        try:
            container = self.client.containers.get(CONTAINER_NAME)
        except docker.errors.NotFound:
            container = None
        return container

    def get_network(self):
        """Return the containers network.

        Returns:
            docker.models.networks.Network: Docker network object.
        """
        try:
            net = self.client.networks.get(NETWORK_NAME)
        except docker.errors.NotFound:
            net = None
        return net

    def get_ip_address(self):
        """Return the containser IP address.

        Returns:
            str: IP address
        """
        container = self.get_container()
        net = self.get_network()
        net_settings = container.attrs["NetworkSettings"]
        addr = net_settings["Networks"][net.name]["IPAddress"]
        return addr

    def execute_user(self, cmd, **add_exec_args):
        """Execute shell command inside the container with your
        current UID and GID.

        Args:
            cmd (str): Command string to be executed.
            add_exec_args: Additional arguments for
                container.exec_run()
        """
        user_info = self._get_user_infos()
        exec_args = {
            "user": "{uid}:{gid}".format(**user_info),
            "workdir": MOUNT_TARGET,
        }
        exec_args.update(add_exec_args)
        self._execute(cmd=cmd, **exec_args)

    def execute_root_shell(self, cmd, **add_exec_args):
        """Execute BASH command inside the container with root
        permissions.

        Args:
            cmd (str): Command string to be executed.
            add_exec_args: Additional arguments for
                container.exec_run()
        """
        self._execute(cmd=cmd, **add_exec_args)

    def prepare(self):
        """Prepare container."""
        self._prepare_user()
        self._prepare_git()

# Developer Notes

## Copyright Header

All files need to contain a copyright header. If you are using Android
Studio, the copyright header is defined in the project settings and
will usually be inserted automatically when creating a new file.

Source code:

```kotlin
/*
 * YacGuide Android application
 *
 * Copyright (C) <year> <Your Name>
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
```

XML (layout) file:
```xml
<!--
 YacGuide Android application

 Copyright (C) <year> <Your Name>

 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <https://www.gnu.org/licenses/>.
 -->
```


## Development Script

For automation of certain development steps, a Python based
[script](../scripts/make) is available. The script uses the projects
[Docker environment](#docker-environment).

:exclamation: **Always start the script from the project root like
`scripts/make <cmd>`.**


### Tool Requirements

The tool chain is tested and designed to work on Linux only. The
following tools must be installed on the user machine to run this
script:

* Python 3 and the pip packages listed in file
  [requirements.txt](../requirements.txt)
* Docker client


## Docker Environment

The Docker environment makes sure that the process of making builds,
releases, tests etc. are done in a defined environment and that the
user only needs to install a minimum tool set on its machine.

The general Docker image is available on [Docker Hub]. On top of this,
the container needs some user-specific setup, which are done during
the container preparation step. These steps are:

* Creating a user with same name, `UID` and `GID` as on the host
* Make minimal Git setup
* Install required tools


### Container Shell

For debugging or interactive work inside the container, you can open a
user shell inside a running container using the command:

```shell
docker exec \
    --user $(id --user):$(id --group) \
    --workdir /mnt/yacguide-build \
    --interactive \
    --tty \
    yacguide-build \
    /bin/bash
```


## App Tests

To run the tests inside the container, you need to execute

```shell
scripts/make tests
```

## Building the App Bundle

During the usual app development process, use Android Studio and
the select the variant `devDebug` for building.

If, e.g. a CI build fails and you cannot reproduce this fail with
Android Studio, you can build the app bundle inside the Docker
environment which is used by the CI. The command

```shell
scripts/make dists
```

builds both, the `dev` and `stable` release which are than available
in the `app/build/outputs/bundle` directory. A special signing setup
(see file `keystore.properties`) is used which is not used for the
actual deployment in the different app stores.


## Documentation

The documentation is written in [GitHub flavored markdown][gfm] and is
stored in the `docs` directory. For each major topic a separate
file exists.


## Preview for Markdown Files

GitHub flavored markdown files can be rendered locally using [grip].
Run the following command to e.g. render this file and access the
resulting page at <http://localhost:6419>.

```shell
grip docs/developer-notes.md
```


## GitHub Page

The GitHub is the projects HTML page which at the moment only exists
for the privacy policy of the app. To render the GitHub page based on
[jekyll] locally, do:

* Run `scripts/make docs --jekyll-serve`
* Access rendered page with address shown by the script output


[Docker Hub]: https://hub.docker.com/repository/docker/yacgroup/yacguide-build
[gfm]: https://github.github.com/gfm
[grip]: https://github.com/joeyespo/grip
[jekyll]: https://jekyllrb.com/

# Index

1. [Contributing to Source Code](#contributing-to-source-code)
   1. [Adding new Files](#adding-new-files)
2. [Docker Environment](#docker-environment)
   1. [Minimal Tool Requirements](#minimal-tool-requirements)
   2. [Getting Started](#getting-started)
   3. [Building the Docker Image](#building-the-docker-image)
   4. [Building the APK locally](#building-the-apk-locally)
3. [Releases](#releases)
   1. [Release Types](#release-types)
   2. [Version Name and Number](#version-number)
   3. [Making Releases](#making-releases)
4. [GitHub Page](#github-page)


# Contributing to Source Code

## Adding new Files

If you create a new file it needs to contain a license header. If you
are using Android Studio, the copyright header is defined in the
project settings and will be inserted automatically when creating a
new file.

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

# Docker Environment

The Docker environment makes sure that the process of making builds,
releases, tests etc. are done in a defined environment and that the
user does not need to install all the required tools on its machine.

The general Docker image is available on [Docker Hub]. On top of this,
the container needs some user-specific setup, which are done during
the container preparation step.

* Create user with same name, UID and GID
* Make minimal Git setup
* Install required tools

## Minimal Tool Requirements

The tool chain is tested and designed to work on Linux only. The
following tools must be installed on the user machine:

* Make
* Docker client

## Getting Started

The command `make help` shows you all necessary commands to work with
the container environment. E.g., if you want to run the tests inside
the container, you need to do the following steps:

1. `make docker-start-new` or, if you already have an existing
  container, use `make docker-start-existing`
2. `make tests`

The second command assumes that you have a running container, which
was started with the first command. Therefore, the second command can
now be executed as long as the container is up and running.

If you want to work interactively inside the container, you first need
to start a container, as in the previous example and then run

`make docker-shell`

to start an interactive shell inside container.

## Building the Docker Image

**This step is only required, if you work at the build environment!**

Clone the repository [yacguide-docker-ci] and run the following
command inside repository root directory:

```shell
docker build -f Dockerfile -t yacgroup/yacguide-build .
```

## Building the APK locally

For building a signed APK the following files are necessary:

* Keystore properties file named `keystore.properties` with the
  following properties defined:

```properties
storePassword=<storePassword>
keyPassword=<keyPassword>
keyAlias=<keyAlias>
storeFile=<keystoreFile>
playServicesFile=<googleServicesAccountFile>
```

* Keystore file (`.jks`)
* Google Service Account file (`.json`)

These files must **not** be committed and will be provided by the CI
for Google Play deployment.

Follow the [Android Studio app signing] documentation for creating a
keystore file.

The Google services account file is only necessary for the actual
Google Play deployment.

### Inside the Docker Environment

For building the APK inside the Docker environment, run the following
command:

```shell
make docker-start-new dists docker-stop
```

### Without using Docker Environment

**NOTE:** This requires that all tools are installed locally e.g. this
is the case, if you have installed Android Studio.

```shell
make dists NO_DOCKER=true
```

### Troubleshooting

If the above build commands fail, the Docker container may still
running. To stop a possible running container run the command:

```shell
make docker-stop
```

If for some reason the container is not being removed, use the
following command to remove them:

```shell
make docker-rm
```

For debugging, you can open a shell inside a running container using
the command:

```shell
make docker-shell
```

# Releases

## Release Types

### Stable

Is released in the [F-Droid] app store. The user interface may change
because the community continuously improves the app. The database will
always be compatible to the previous version, however, if a database
change is necessary, the user will be informed how he can convert
e.g. his tour book.

### Dev

The development version contains regular snapshots which may lead to
unexpected changes without notice. It can be installed alongside the
official `YacGuide` app and is available at [F-Droid] under the name
`YacGuide Dev`. It uses an independent internal database, hence e.g.
tour book entries from the stable release cannot be accessed
automatically from the development version and vice versa. However
usually it is possible to exchange the data via a manual export/import
step.

## Version Number

### Stable Version

The version number follows the [semantic version scheme] and consists
of three digits `X.Y.Z`.

* `X` - Increased, if incompatible changes are made.
* `Y` - Increased by compatible feature changes.
* `Z` - Increased for bug fixes.

### Development Version

The development version uses the date in the format `YYYYMMDD` as
version number.

## Making Releases

The release process is based on Git tags.

The [F-Droid] server checks for new tags in the repo on a regular
basis. If there is new matching tag, the server starts the build and
sign process and makes the app available in the app store.

The deployment of the app to the Google Play store is done by the CI,
if a matching release tag is pushed to the GitHub repository.

To create a release tag, do the following steps:

1. Change into the `master` branch and sync your branch with the
   remote repository.
2. Make sure that your Git status is clean, e.g. that you have no
   uncommitted or untracked items.
3. Prepare the Docker container, if necessary
   (see [Docker Environment](#docker-environment))
4. Run the following command to create and commit the release tag:
```shell
make release-dev  # Development release
make release-stable VERSION=X.Y.Z  # Stable release
```

## GitHub Page

To build the GitHub page locally, you need to do the following steps
inside the projects root directory:

* Prepare the Docker container, if necessary
  (see [Docker Environment](#docker-environment))
* Run `make jekyll-serve`
* Access generated GitHub page at <http://127.0.0.1:4000>

[yacguide-docker-ci]: https://github.com/YacGroup/yacguide-docker-ci
[F-Droid]: https://f-droid.org
[semantic version scheme]: http://semver.org/
[Docker Hub]: https://hub.docker.com/repository/docker/yacgroup/yacguide-build
[Android Studio app signing]: https://developer.android.com/studio/publish/app-signing

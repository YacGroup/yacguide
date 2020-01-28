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

## Building the Docker Image

**This step is only required, if you work at the build environment!**

Clone the repository
[yacguide-docker-ci](https://github.com/YacGroup/yacguide-docker-ci)
and run the following command inside repository root directory:

```shell
docker build -f Dockerfile -t yacgroup/yacguide-build .
```

## Building the APK locally

Copy the `keystore.jks` file from your private location to
the project directory but **DO NOT** commit this file.

The parameters `<storepass>` and `<keypass>` are the keystore and key
passwords for signing the APK file.

Run the following commands within the repository root.

### Inside the Docker Environment

```shell
make docker-start-new dist docker-stop STOREPASS="<storepass>" KEYPASS="<keypass>"
```

### Without using Docker Environment

**NOTE:** This requires that all tools are installed locally.

```shell
make dist NO_DOCKER=true STOREPASS="<storepass>" KEYPASS="<keypass>"
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

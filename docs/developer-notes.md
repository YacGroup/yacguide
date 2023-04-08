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
[script](../scripts/make) is available.

:exclamation: **Always start the script from the project root like
`scripts/make <cmd>`.**


### Tool Requirements

The tool chain is tested and designed to work on Linux only. The
following tools must be installed on the user machine to run this
script:

* Python 3 and the pip packages listed in file
  [requirements.txt](../requirements.txt)


## App Tests

To run the tests, you need to execute

```shell
# Run unit tests for all variants
./gradlew test

# Runs all instrumentation tests on currently connected devices
./gradlew connectedCheck
```

## Building the App Bundle

During the usual app development process, use Android Studio and the
select the variant `<dev|stable>Debug` for building. The builds are
than available in the `app/build/outputs/bundle` directory. A special
signing setup (see file `keystore.properties`) is used which is not
used for the actual deployment in the different app stores.


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

GitHub serves the projects HTML page. To render the GitHub page based
on [jekyll] locally, run the following commands:

* Change to `docs` directory
* Build Docker container

  ```
  docker build -t yacguide-docs .
  ```
* Build HTML page

  ```
  docker run \
      --interactive \
      --tty \
      --rm \
      --network host \
      --publish 4000:4000 \
      --name yacguide-docs \
      yacguide-docs \
      bundle exec jekyll serve
  ```
* Access rendered page with address shown by the script output


[gfm]: https://github.github.com/gfm
[grip]: https://github.com/joeyespo/grip
[jekyll]: https://jekyllrb.com/

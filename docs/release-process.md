# Release Process

The release process is based on Git tags and necessary for publishing
the app in the [F-Droid] and [Google Play] store.

The [F-Droid] server checks for new tags in the repo on a regular
basis. If there is new matching tag, the server starts the build and
signing process and makes the app available in the app store with a
delay of around four days.

The actual deployment of the app to the [Google Play] store is done by
the CI, if a matching release tag is pushed to the GitHub repository.
The version will be available in the store with a delay of around one
day.

To create a release tag, do the following steps:

1. Change into the `master` branch and sync your branch with the
   remote repository.
2. For a stable release, add or update file `vX.Y.Z.md` in directory
   [release notes directory](release-notes). Describe the changes made
   since the last release and reference the corresponding issues, if
   exists, as shown the example below:
   ```markdown
   ## Bugfixes

   * Fixed app crash when trying to import the tour book on Android 10 (#166)
   * Fixed bad formatting of route description (#153)

   ## Improvements

   * Show alternative sector and route names (#167)
   * Show progress during database update (#106)
   ```
3. Make sure that your Git status is clean, e.g. that you have no
   uncommitted or untracked items.
4. Run one of the following commands to create either a development or
   stable release and follow the instruction of the script output.
   ```shell
   scripts/make release --type dev [--version YYYYMMDD]
   scripts/make release --type stable --version X.Y.Z
   ```
5. Wait that the CI passes.
6. In case of a stable release, attach the content of the release note
   file to the GitHub tag.

[F-Droid]: https://f-droid.org
[Google Play]: https://play.google.com

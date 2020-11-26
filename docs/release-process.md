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
2. Make sure that your Git status is clean, e.g. that you have no
   uncommitted or untracked items.
3. Run one of the following commands to create either a development or
   stable release and follow the instruction of the script output.
   ```shell
   scripts/make release --type dev [--version YYYYMMDD]
   scripts/make release --type stable --version X.Y.Z
   ```
4. Wait that the CI passes.

[F-Droid]: https://f-droid.org
[Google Play]: https://play.google.com

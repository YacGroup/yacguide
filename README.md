# [YacGuide] - Yet Another Climbing Guide

[![CI](https://github.com/YacGroup/yacguide/actions/workflows/ci.yaml/badge.svg)](https://github.com/YacGroup/yacguide/actions/workflows/ci.yaml)
[![Releases](https://img.shields.io/github/release/yacgroup/yacguide.svg)](https://github.com/yacgroup/yacguide/releases/latest)

[<img src="https://play.google.com/intl/en_us/badges/images/generic/en_badge_web_generic.png"
      alt="Get it on Google Play"
      height="60">](https://play.google.com/store/apps/details?id=com.yacgroup.yacguide)
[<img src="https://fdroid.gitlab.io/artwork/badge/get-it-on.png"
      alt="Get it on F-Droid"
      height="60">](https://f-droid.org/en/packages/com.yacgroup.yacguide/)

Offline tool for
[sandsteinklettern.de](http://www.sandsteinklettern.de).

**Features:**

 * Offline access to climbing route information
 * Tour book

## Contributing

See our [contributing doc](CONTRIBUTING.md) for information.


## Release Types

### Stable

Is released in the [Google Play Store][YacGuide GPlay].
The user interface may change because the community
continuously improves the app. The database will always be compatible
to the previous version, however, if a database change is necessary,
the user will be informed how he can convert e.g. its tour book.

The version number follows the [semantic version scheme] and consists
of three digits `X.Y.Z`.

* `X` - Increased, if incompatible changes are made.
* `Y` - Increased by compatible feature changes.
* `Z` - Increased for bug fixes.

### Development (Dev)

The development version contains regular snapshots which may lead to
unexpected changes without notice. It can be installed alongside the
stable release and is available at [F-Droid][YacGuide Dev F-Droid] as
well as in the [Google Play Store][YacGuide Dev GPlay] under the name
`YacGuide Dev`. It uses an independent internal database, hence e.g.
tour book entries from the stable release cannot be accessed
automatically from the development version and vice versa. However it
is possible to exchange the data via a manual export/import step.

The development version uses the date in the format `YYYYMMDD` as
version number.


[YacGuide]: https://yacgroup.github.io/yacguide/
[F-Droid]: https://f-droid.org
[YacGuide Dev F-Droid]: https://f-droid.org/en/packages/com.yacgroup.yacguide.dev/
[YacGuide GPlay]: https://play.google.com/store/apps/details?id=com.yacgroup.yacguide
[YacGuide Dev GPlay]: https://play.google.com/store/apps/details?id=com.yacgroup.yacguide.dev
[semantic version scheme]: http://semver.org/
[Android Studio app signing]: https://developer.android.com/studio/publish/app-signing

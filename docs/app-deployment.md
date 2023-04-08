# App Deployment

## App Bundle for Google Play

:exclamation: **This step is usually not necessary for releasing the
app.**

For (manually) building a signed app bundle the following files are
necessary:

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

These files must **not** be committed and are provided by the CI for
Google Play deployment.

To build all app bundles, run command

```shell
./gradlew bundleRelease
```

which builds both, the `dev` and `stable` release which are than
available in the `app/build/outputs/bundle` directory.


## App for F-Droid Store

The signing key is handled by the F-Droid server and is not
accessible. A official release can only be build by the steps
described in the [release process].


## Further Resources

* [Android Studio app signing guide]

[Android Studio app signing guide]: https://developer.android.com/studio/publish/app-signing
[release process]: release-process.md

#! /usr/bin/env bash

storepass=$1
keypass=$2

### Clean-up
rm -f /mnt/yacguide-build/dist/*.apk

### Building unsigned APK
./gradlew \
    --gradle-user-home /mnt/yacguide-build/.gradle/ \
    clean \
    assembleRelease
mkdir -p /mnt/yacguide-build/dist
cp -f ./app/build/outputs/apk/release/app-release-unsigned.apk /mnt/yacguide-build/dist

### Signing APK
jarsigner \
    -verbose \
    -keystore keystore.jks \
    -storepass $storepass \
    -keypass $keypass \
    -signedjar /mnt/yacguide-build/dist/app-release-signed.apk \
    /mnt/yacguide-build/dist/app-release-unsigned.apk \
    key0

### ZIP alignment
# https://developer.android.com/studio/command-line/zipalign
${ANDROID_HOME}/build-tools/${ANDROID_BUILD_TOOLS}/zipalign \
               4 \
               /mnt/yacguide-build/dist/app-release-signed.apk \
               /mnt/yacguide-build/dist/yacguide.apk

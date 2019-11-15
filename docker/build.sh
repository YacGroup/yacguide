#! /usr/bin/env bash

storepass="$1"
keypass="$2"
if [ -z ${3+x} ]; then
    runDocker=true
fi

dockerImage="yacgroup/yacguide-build"
dockerContainer="yacguide-build"
dockerMountTarget="/mnt/yacguide"
releaseDir="$(pwd)/app/build/outputs/apk/release"
releaseApk="${releaseDir}/yacguide.apk"
userId=$(id --user)
groupId=$(id --group)
userName=$(id --user --name)
groupName=$(id --group --name)

if [ ${runDocker} ]; then
    ### Run this script in the Docker container context
    # Start new container in background and mount local Git repository
    # into the container.
    echo "Starting container ..."
    docker run \
        --detach \
        --rm \
        --name ${dockerContainer} \
        --mount type=bind,src="$(pwd)",dst="${dockerMountTarget}" \
        ${dockerImage} /bin/bash -c "tail -f /dev/null" || exit 1
    # Create user inside the container with the same UID and GID as
    # the current user. This makes sure that the files and directories
    # of local Git repository are modified with the same permissions.
    echo "Creating build user inside the container ..."
    groupaddCmd="groupadd --gid ${groupId} ${groupName}"
    useraddCmd="useradd \
                    --create-home \
                    --uid ${userId} \
                    --gid ${groupId} \
                    ${userName}"
    docker exec \
        ${dockerContainer} \
        /bin/bash -c "${groupaddCmd} && ${useraddCmd}" || exit 2
    # Run the build script inside the container as the previously
    # created user.
    echo "Running the actual build inside the container ..."
    docker exec \
        --user ${userId}:${groupId} \
        --workdir "${dockerMountTarget}" \
        ${dockerContainer} \
        $0 $storepass $keypass false
    echo "Stopping container ..."
    docker stop --time 0 ${dockerContainer} || exit 3
else
    echo "Building unsigned APK ..."
    ./gradlew \
        --gradle-user-home $(pwd)/.gradle/ \
        clean \
        assembleRelease || exit 4

    echo "Signing APK ..."
    jarsigner \
        -verbose \
        -keystore keystore.jks \
        -storepass "$storepass" \
        -keypass "$keypass" \
        -signedjar ${releaseDir}/app-release-signed.apk \
        ${releaseDir}/app-release-unsigned.apk \
        key0 || exit 5

    # https://developer.android.com/studio/command-line/zipalign
    echo "ZIP alignment ..."
    ${ANDROID_HOME}/build-tools/${ANDROID_BUILD_TOOLS}/zipalign \
        -f \
        4 \
        ${releaseDir}/app-release-signed.apk \
        ${releaseApk} || exit 6
    echo "APK file: ${releaseApk}"
fi

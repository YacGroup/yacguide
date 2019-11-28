# This Dockerfile creates a static build image for the CI.
# The image will available at the Docker Hub registry.

FROM openjdk:8-jdk

# Install required OS packages
RUN \
   apt-get update && \
   apt-get install --yes git wget apt-utils unzip make && \
   apt-get autoclean

# Matched version in `app/build.gradle`
ENV ANDROID_COMPILE_SDK "28"
# Matched version in `app/build.gradle`
ENV ANDROID_BUILD_TOOLS "28.0.3"
# Version from https://developer.android.com/studio/releases/sdk-tools
ENV ANDROID_SDK_TOOLS "26.1.1"
ENV ANDROID_HOME /usr/local/android-sdk
ENV PATH="${PATH}:${ANDROID_HOME}/platform-tools/"

# Install Android SDK tools
RUN mkdir -p ${ANDROID_HOME}
WORKDIR ${ANDROID_HOME}
# FIXME: The version of the file do not match the version at
#        https://developer.android.com/studio/releases/sdk-tools
RUN wget --quiet --output-document=android-sdk.zip https://dl.google.com/android/repository/sdk-tools-linux-4333796.zip
RUN unzip android-sdk.zip
WORKDIR /root
RUN echo y | ${ANDROID_HOME}/tools/android --use-sdk-wrapper update sdk --no-ui --all --filter android-${ANDROID_COMPILE_SDK}
RUN echo y | ${ANDROID_HOME}/tools/android --use-sdk-wrapper update sdk --no-ui --all --filter platform-tools
RUN echo y | ${ANDROID_HOME}/tools/android --use-sdk-wrapper update sdk --no-ui --all --filter build-tools-${ANDROID_BUILD_TOOLS}

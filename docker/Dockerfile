# This Dockerfile creates a static build image for CI.

FROM openjdk:8-jdk

# Distribution directory that will be visible to outside the
# container.
ENV BUILD_DIR /mnt/yacguide-build

# Matched version in `app/build.gradle`
ENV ANDROID_COMPILE_SDK "28"
# Matched version in `app/build.gradle`
ENV ANDROID_BUILD_TOOLS "28.0.3"
# Version from https://developer.android.com/studio/releases/sdk-tools
ENV ANDROID_SDK_TOOLS "26.1.1"
ENV ANDROID_HOME /usr/local/android-sdk
ENV PATH="${PATH}:${ANDROID_HOME}/platform-tools/"

# Install OS packages
RUN \
   apt-get update && \
   apt-get install --yes git wget apt-utils unzip && \
   apt-get autoclean

# Add build user
RUN useradd -m yac

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

# Prepare deployment directory
RUN \
   mkdir -p ${BUILD_DIR} && \
   chown yac.yac ${BUILD_DIR} 
VOLUME ${BUILD_DIR}

# Prepare build
WORKDIR /home/yac
COPY --chown=yac:yac . yacguide

USER yac
WORKDIR /home/yac/yacguide
#CMD ["./docker/build.sh"]

## Building the Docker Image

**This step is only required, if you work at the build environment!**

Build the Docker image inside the project root directory.

```shell
docker build -f docker/Dockerfile -t chrgernoe/yacguide .
```

## Building a APK locally

Run the following commands within the repository root.

Copy the `keystore.jks` file from your private location to
the project directory but **DO NOT** commit this file.

Run the build inside the container:

```shell
docker/build.sh <storepass> <keypass>
```

The parameters `<storepass>` and `<keypass>` are the keystore and key
passwords for signing the APK file.

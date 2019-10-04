## Build APK within Docker Container

Run the following commands within the repository root.

Build the image:

```shell
docker build -f docker/Dockerfile -t yacguide .
```

Create docker volume. Needs to done once.

```shell
docker volume create yacguide
```

Run the build inside the container:

```shell
docker run --volume yacguide:/mnt/yacguide-build yacguide docker/build.sh <storepass> <keypass>
```

The parameters `<storepass>` and `<keypass>` are the keystore and key passwords or signing the APK file. 

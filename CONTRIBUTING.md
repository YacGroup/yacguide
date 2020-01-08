## Building the Docker Image

**This step is only required, if you work at the build environment!**

Build the Docker image inside the project root directory.

```shell
docker build -f docker/Dockerfile -t yacgroup/yacguide-build .
```

## Building the APK locally

Copy the `keystore.jks` file from your private location to
the project directory but **DO NOT** commit this file.

The parameters `<storepass>` and `<keypass>` are the keystore and key
passwords for signing the APK file.

Run the following commands within the repository root.

### Inside the Docker Environment

```shell
make docker-start-new dist docker-stop STOREPASS="<storepass>" KEYPASS="<keypass>"
```

### Without using Docker Environment

**NOTE:** This requires that all tools are installed locally.

```shell
make dist NO_DOCKER=true STOREPASS="<storepass>" KEYPASS="<keypass>"
```

### Troubleshooting

If the above build commands fail, the Docker container may still
running. To stop a possible running container run the command:

```shell
make docker-stop
```

If for some reason the container is not being removed, use the
following command to remove them:

```shell
make docker-rm
```

For debugging, you can open a shell inside a running container using
the command:

```shell
make docker-shell
```

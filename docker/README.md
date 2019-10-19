## Yacguide Docker Build Environment

This is the Docker image for building the `yacguide` Android app.

See <https://github.com/paetz/yacguide> for more information.

For manually building the image run following command inside root of
your local `yacguide` Git repository:

``` shell
docker build -f docker/Dockerfile -t yacguide .
```

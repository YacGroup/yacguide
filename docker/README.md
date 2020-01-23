## YacGuide Docker Build Environment

This is the Docker image for building the `YacGuide` Android app.

See <https://github.com/YacGroup/YacGuide> for more information.

For manually building the image run following command inside root of
your local Git repository:

``` shell
docker build -f docker/Dockerfile -t yacgroup/yacguide-build .
```

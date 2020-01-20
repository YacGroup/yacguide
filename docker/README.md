## YAC Guide Docker Build Environment

This is the Docker image for building the `YAC Guide` Android app.

See <https://github.com/yacgroup/yacguide> for more information.

For manually building the image run following command inside root of
your local Git repository:

``` shell
docker build -f docker/Dockerfile -t yacgroup/yacguide-build .
```

FROM chrgernoe/yacguide:latest

# Copy local Git repository files to the container.
WORKDIR /home/yac
COPY --chown=yac:yac . yacguide

USER yac
WORKDIR /home/yac/yacguide

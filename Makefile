# --------------------------------------------------------------------
# Docker parameters
# --------------------------------------------------------------------
DOCKER_IMG_VER := 20201005
DOCKER_IMG := yacgroup/yacguide-build:$(DOCKER_IMG_VER)
DOCKER_CONTAINER := yacguide-build
DOCKER_MOUNT_TARGET := /mnt/yacguide-build
USER_ID := $(shell id --user)
GROUP_ID := $(shell id --group)
USER_NAME := $(shell id --user --name)
GROUP_NAME := $(shell id --group --name)
GROUP_ADD_CMD := groupadd --gid $(GROUP_ID) $(GROUP_NAME)
USER_ADD_CMD := useradd \
	                 --create-home \
                    --uid $(USER_ID) \
                    --gid $(GROUP_ID) \
                    $(USER_NAME)
DOCKER_EXEC_CMD := docker exec \
                      --user $(USER_ID):$(GROUP_ID) \
                      --workdir $(DOCKER_MOUNT_TARGET)

# --------------------------------------------------------------------
# Build parameters
# --------------------------------------------------------------------
GRADLE_USER_HOME = $$(pwd)/.gradle/
ifndef NO_DOCKER
# Run the build scripts inside the container under the current user.
EXEC_CMD := $(DOCKER_EXEC_CMD) $(DOCKER_CONTAINER) /bin/bash -c
else
EXEC_CMD := /bin/bash -c
endif

# --------------------------------------------------------------------
# General targets
# --------------------------------------------------------------------
.PHONY: help
help::
	@echo "Makefile for general tasks"
	@echo ""
	@echo "gmake <TARGET> <PARAMETERS>"
	@echo ""
	@echo "Parameters:"
	@echo "  NO_DOCKER=true - Do not run commands inside Docker container."
	@echo "  SHELL_CMD=\"<shell command>\" - Shell command to be run (optional)"
	@echo "  DOCKER_IMG_VER=<version> - Optional Docker image version. (default: $(DOCKER_IMG_VER))"
	@echo ""
	@echo "Targets:"

help::
	@echo "  dists - build releases or all flavors"

.PHONY: dists
dists:
	@echo "Building and signing APKs ..."
	$(EXEC_CMD) "./gradlew \
		--gradle-user-home $(GRADLE_USER_HOME) \
		clean \
		bundleDevRelease \
		bundleStableRelease"

help::
	@echo "  distclean - clean everything not under version control"

.PHONY: distclean
distclean:
	git clean -Xfd

help::
	@echo "  clean - clean build directory"

.PHONY: clean
clean:
	$(RM) -r app/build

help::
	@echo "  run-shell-cmd - run a shell command provided by parameter"
	@echo "                  SHELL_CMD inside the Docker container."
	@echo "                  If NO_DOCKER=true, the command is"
	@echo "                  executed locally."

.PHONY: run-shell-cmd
run-shell-cmd:
	$(EXEC_CMD) '$(SHELL_CMD)'

# --------------------------------------------------------------------
# Docker command
# --------------------------------------------------------------------
help::
	@echo "  docker-start-new - start new Docker container"

.PHONY: docker-start-new
docker-start-new: docker-stop docker-rm docker-run docker-prep

help::
	@echo "  docker-start-existing - start existing Docker container"

.PHONY: docker-start-existing
docker-start-existing:
	@echo "Starting existing container '$(DOCKER_CONTAINER)' ..."
	docker start $(DOCKER_CONTAINER)

# Start new container in background and mount local Git repository
# into the container. TCP port 4000 is published to the host for
# accessing the Jekyll web server.
.PHONY: docker-run
docker-run:
	@echo "Starting new container ..."
	docker run \
		--detach \
		--name $(DOCKER_CONTAINER) \
		--mount type=bind,src="$(PWD)",dst="$(DOCKER_MOUNT_TARGET)" \
		--network host \
		--publish 127.0.0.1:4000:4000/tcp \
		$(DOCKER_IMG) /bin/bash -c 'tail -f /dev/null'

.PHONY: docker-prep
docker-prep: docker-prep-user docker-prep-git \
jekyll-setup

# Create a user inside the container with the same UID and GID as the
# current user. This makes sure that the files and directories of
# local Git repository are modified with the same permissions.
.PHONY: docker-prep-user
docker-prep-user:
	@echo "Preparing user and group inside container ..."
	docker exec \
		$(DOCKER_CONTAINER) \
		/bin/bash -c '$(GROUP_ADD_CMD) && $(USER_ADD_CMD)'

# Setup Git inside the container so that commits are possible.
.PHONY: docker-prep-git
docker-prep-git:
	@echo "Preparing Git inside container ..."
	$(DOCKER_EXEC_CMD) \
		--env GIT_CFG_USER_EMAIL="$$(git config --global --get user.email)" \
		--env GIT_CFG_USER_NAME="$$(git config --global --get user.name)" \
		$(DOCKER_CONTAINER) \
		/bin/bash -c '\
git config --global user.email "$$GIT_CFG_USER_EMAIL" && \
git config --global user.name "$$GIT_CFG_USER_NAME"'

help::
	@echo "  docker-stop - stop Docker container"

# Don't care, if the container doesn't exist.
.PHONY: docker-stop
docker-stop:
	@echo "Stopping container ..."
	-docker stop --time 0 $(DOCKER_CONTAINER)

help::
	@echo "  docker-rm - remove Docker container"

# Don't care, if the container doesn't exist.
.PHONY: docker-rm
docker-rm:
	@echo "Removing container '$(DOCKER_CONTAINER)'."
	-docker container rm $(DOCKER_CONTAINER)

help::
	@echo "  docker-shell - start interactive shell in Docker container"

.PHONY: docker-shell
docker-shell:
	$(DOCKER_EXEC_CMD) \
		--interactive \
		--tty \
		$(DOCKER_CONTAINER) \
		/bin/bash

# --------------------------------------------------------------------
# Testing
# --------------------------------------------------------------------
help::
	@echo "  tests - run tests"

.PHONY: tests
tests:
	$(EXEC_CMD) "./gradlew \
		--gradle-user-home $(GRADLE_USER_HOME) \
		test"

# --------------------------------------------------------------------
# GitHub page commands
# --------------------------------------------------------------------
# Install required Ruby Gems for Jekyll
.PHONY: jekyll-setup
jekyll-setup:
	@echo "Running Jekyll setup ..."
	$(EXEC_CMD) "cd docs && bundle install --path vendor/bundle"

help::
	@echo "  jekyll-serve - run Jekyll web server"

.PHONY: jekyll-serve
jekyll-serve:
	@echo "Running Jekyll web server ..."
	$(EXEC_CMD) "cd docs && bundle exec jekyll serve"

# --------------------------------------------------------------------
# Commands to make releases using the container
# --------------------------------------------------------------------
help::
	@echo "  release-dev - Make development release (commit and tag)"

.PHONY: release-dev
release-dev:
	@echo "Making development release ..."
	$(EXEC_CMD) "scripts/create_release --release-type dev"

help::
	@echo "  release-stable - Make stable release (commit and tag)."
	@echo "                   Specify the version using parameter VERSION."

.PHONY: release-stable
release-stable:
	@echo "Making stable release ..."
	$(EXEC_CMD) "scripts/create_release \
		--release-type stable \
		--version $(VERSION)"

help::
	@echo "  deploy-play-dev - Deploy development release to Google Play"

.PHONY: deploy-play-dev
deploy-play-dev:
	@echo "Deploying development release to Google Play ..."
	$(EXEC_CMD) "./gradlew \
		--gradle-user-home $(GRADLE_USER_HOME) \
		publishDevReleaseBundle"

help::
	@echo "  deploy-play-stable - Deploy stable release to Google Play"

.PHONY: deploy-play-stable
deploy-play-stable:
	@echo "Deploying stable release to Google Play ..."
	$(EXEC_CMD) "./gradlew \
		--gradle-user-home $(GRADLE_USER_HOME) \
		publishStableReleaseBundle"

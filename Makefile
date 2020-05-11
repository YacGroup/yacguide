# --------------------------------------------------------------------
# Docker parameters
# --------------------------------------------------------------------
DOCKER_IMAGE := yacgroup/yacguide-build:20200329
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
KEYSTORE_FILE := keystore.jks
FLAVOR := stable
# Make directories relative because we are mounting the current
# working directory to another path in the container.
RELEASE_DIR := app/build/outputs/apk/$(FLAVOR)/release
APK_FILE_UNSIGNED := $(RELEASE_DIR)/app-$(FLAVOR)-release-unsigned.apk
APK_FILE_SIGNED := $(RELEASE_DIR)/app-$(FLAVOR)-release-signed.apk
APK_FILE_DEPLOY := $(RELEASE_DIR)/yacguide-$(FLAVOR).apk
ifndef NO_DOCKER
# Run the build scripts inside the container under the current user.
EXEC_CMD := $(DOCKER_EXEC_CMD) $(DOCKER_CONTAINER) /bin/bash -c
else
EXEC_CMD := /bin/bash -c
endif

# --------------------------------------------------------------------
# Internal variables
# --------------------------------------------------------------------
FLAVOR_CAP := $(shell echo $(FLAVOR) | sed -e 's/\b\(.\)/\u\1/g')

# --------------------------------------------------------------------
# General targets
# --------------------------------------------------------------------

.PHONY: help
help::
	@echo "Build and sign APK file."
	@echo ""
	@echo "gmake <TARGET> <PARAMETERS>"
	@echo ""
	@echo "Target 'apk-sign' requires parameters 'STOREPASS' and 'KEYPASS'."
	@echo ""
	@echo "Parameters:"
	@echo "  NO_DOCKER=true - Do not run commands inside Docker container."
	@echo "  STOREPASS=\"<password>\" - Keystore password (optional)"
	@echo "  KEYPASS=\"<password>\" - Key password (optional)"
	@echo "  FLAVOR=stable|dev - App flavor (optional). Default: stable"
	@echo "  SHELL_CMD=\"<shell command>\" - Shell command to be run (optional)"
	@echo ""
	@echo "Targets:"

help::
	@echo "  dist - make distribution (run all apk-* targets)"

.PHONY: dist
dist: apk-build apk-sign apk-zipalign

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
# APK build commands
# --------------------------------------------------------------------
help::
	@echo "  apk-build - build unsigned APK file"

.PHONY: apk-build
apk-build: $(APK_FILE_UNSIGNED)

$(APK_FILE_UNSIGNED):
	@echo "Building unsigned APK ..."
	$(EXEC_CMD) './gradlew \
		--gradle-user-home $$(pwd)/.gradle/ \
		clean \
		assemble$(FLAVOR_CAP)Release'

help::
	@echo "  apk-sign - sign APK file"

.PHONY: apk-sign
apk-sign: $(APK_FILE_SIGNED)

$(APK_FILE_SIGNED): $(APK_FILE_UNSIGNED)
	@echo "Signing APK ..."
	$(EXEC_CMD) \
		'$${ANDROID_HOME}/build-tools/$${ANDROID_BUILD_TOOLS}/apksigner \
			sign \
			--ks $(KEYSTORE_FILE) \
			--ks-pass pass:$(STOREPASS) \
			--key-pass pass:$(KEYPASS) \
			--in $< \
			--out $@'

help::
	@echo "  apk-zipalign - zipalign signed APK file"

.PHONY: apk-zipalign
apk-zipalign: $(APK_FILE_DEPLOY)

# https://developer.android.com/studio/command-line/zipalign
$(APK_FILE_DEPLOY): $(APK_FILE_SIGNED)
	@echo "ZIP alignment ..."
	$(EXEC_CMD) \
		'$${ANDROID_HOME}/build-tools/$${ANDROID_BUILD_TOOLS}/zipalign \
			-f 4 $< $@'

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
		$(DOCKER_IMAGE) /bin/bash -c 'tail -f /dev/null'

.PHONY: docker-prep
docker-prep: docker-prep-user docker-prep-git fastlane-setup \
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
# Fastlane commands
# --------------------------------------------------------------------
# Install required Ruby Gems for Fastlane
.PHONY: fastlane-setup
fastlane-setup:
	@echo "Running Fastlane setup ..."
	$(EXEC_CMD) "bundle install --path vendor/bundle"

help::
	@echo "  tests - run tests"

.PHONY: tests
tests:
	$(EXEC_CMD) "bundle exec fastlane test"

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
	@echo "  release-dev - Make development release (tag and commit)"

.PHONY: release-dev
release-dev:
	@echo "Making development release ..."
	$(EXEC_CMD) "bundle exec fastlane create_dev_release"

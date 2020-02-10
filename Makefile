# --------------------------------------------------------------------
# Docker parameters
# --------------------------------------------------------------------
DOCKER_IMAGE := yacgroup/yacguide-build
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
APK_RUN_CMD := $(DOCKER_EXEC_CMD) $(DOCKER_CONTAINER) /bin/bash -c
else
APK_RUN_CMD := /bin/bash -c
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
	@echo "  STOREPASS=\"<PASSOWRD>\" - Keystore password (optional)"
	@echo "  KEYPASS=\"<PASSOWRD>\" - Key password (optional)"
	@echo "  FLAVOR=stable|dev - App flavor (optional). Default: stable"
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

# --------------------------------------------------------------------
# APK build commands
# --------------------------------------------------------------------
help::
	@echo "  apk-build - build unsigned APK file"

.PHONY: apk-build
apk-build: $(APK_FILE_UNSIGNED)

$(APK_FILE_UNSIGNED):
	@echo "Building unsigned APK ..."
	$(APK_RUN_CMD) './gradlew \
		--gradle-user-home $$(pwd)/.gradle/ \
		clean \
		assemble$(FLAVOR_CAP)Release'

help::
	@echo "  apk-sign - sign APK file"

.PHONY: apk-sign
apk-sign: $(APK_FILE_SIGNED)

$(APK_FILE_SIGNED): $(APK_FILE_UNSIGNED)
	@echo "Signing APK ..."
	$(APK_RUN_CMD) \
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
	$(APK_RUN_CMD) \
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
# into the container.
.PHONY: docker-run
docker-run:
	@echo "Starting new container ..."
	docker run \
		--detach \
		--name $(DOCKER_CONTAINER) \
		--mount type=bind,src="$(PWD)",dst="$(DOCKER_MOUNT_TARGET)" \
		$(DOCKER_IMAGE) /bin/bash -c 'tail -f /dev/null'

# Create a user inside the container with the same UID and GID as the
# current user. This makes sure that the files and directories of
# local Git repository are modified with the same permissions.
.PHONY: docker-prep
docker-prep:
	@echo "Preparing container ..."
	docker exec \
		$(DOCKER_CONTAINER) \
		/bin/bash -c '$(GROUP_ADD_CMD) && $(USER_ADD_CMD)'

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

# Start an interactive shell in the Docker container
.PHONY: docker-shell
docker-shell:
	$(DOCKER_EXEC_CMD) \
		--interactive \
		--tty \
		$(DOCKER_CONTAINER) \
		/bin/bash

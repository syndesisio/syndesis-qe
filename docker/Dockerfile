#================================================================================================================================
# Default version, for now, is 1.12.x, `docker build -t syndesisqe/syndesisqe-tests:1.12.x .`
# With the BUILD ARG, you can specify which version of Syndesis QE and Syndesis will be used in the final image
# e.g. `docker build -t docker.io/syndesisqe/syndesisqe-tests:<tag> --build-arg SYNDESIS_BRANCH_VERSION=1.10.x --build-arg SYNDESIS_QE_BRANCH_VERSION=1.10.x .`
# If you want to use your Syndesis/SyndesisQE fork during building, you can specify a build arg SYNDESIS_BRANCH_OWNER or SYNDESIS_QE_BRANCH_OWNER
# e.g. For building an image for testing changes in the code base which are committed only in the owner repo:
# `docker build -t docker.io/syndesisqe/syndesisqe-tests:<tag> --build-arg SYNDESIS_QE_BRANCH_VERSION=FUSEQE-9071_Containerizing-test-suite --build-arg SYNDESIS_QE_BRANCH_OWNER=mkralik3 .`


# You have to specify the cluster URL, NAMESPACE and ui credentials in the environment variables. When you don't specify tags, only the smoke test will be running.
# You can specify multiple test tags using cucumber notation via TAGS environment variable  like `-e TAGS='@smoke OR @database'`
# ! Be aware that the tests which use 3rd party services specified in credentials.json are not supported yet !
# The image contains only minimal credential.json in order to the tests which use self-deployed services work (AMQ broker, MongoDB).
# You have also share memory for container, otherwise the firefox crash. (`--shm-size=2g`)
## e.g.
#docker run \
#   -e URL='https://192.168.42.236:8443' \
#   -e ADMIN_USERNAME='admin' \
#   -e ADMIN_PASSWORD='admin' \
#   -e NAMESPACE='mkralik' \
#   --shm-size=2g \
#   syndesisqe/syndesisqe-tests:1.12.x
#
#
# For debugging, you have to expose port 5005 by `-p 5005:5005`
# For running tests with 3rd party services, use CREDENTIALS_URL env `-e CREDENTIALS_URL=http://.../credentials.json`
# If you want to see the test results (in case the test failed) after the container is finished, you have to mount any folder from
# your local machine (/tmp/syndesisQe in the example) to the container. `-v <your folder in localhost>:/test-run-results:Z`
#================================================================================================================================

FROM alpine/git as gitRepoSyndesis

ARG SYNDESIS_BRANCH_OWNER=syndesisio
ARG SYNDESIS_BRANCH_VERSION=1.12.x
ARG INSTALL_TAG=1.12

RUN git clone -b ${SYNDESIS_BRANCH_VERSION} https://github.com/${SYNDESIS_BRANCH_OWNER}/syndesis.git /syndesis \
	&& git clone -b ${INSTALL_TAG} https://github.com/${SYNDESIS_BRANCH_OWNER}/fuse-online-install.git /fuse-online-install

#================================================================================================================================
## The rest tests and utils modules use some Syndesis artifacts which are not available, it needs to build them and copy to the final image
# for some reason, the syndesis build doesn't work on alpine image
FROM maven:3.6-jdk-8-slim as buildSyndesis
COPY --from=gitRepoSyndesis /syndesis /syndesis
WORKDIR /syndesis/app
## Syndesis build needs git for some reason
## Build only dependencies that the test suite needs
RUN apt-get update -y \
	&& apt-get install -y git \
	&& mvn clean install -P flash --projects :common-model,:server-endpoint --also-make

#================================================================================================================================
FROM alpine/git as gitRepoSyndesisQe

ARG SYNDESIS_QE_BRANCH_OWNER=syndesisio
ARG SYNDESIS_QE_BRANCH_VERSION=1.12.x

ARG APICURIO_QE_BRANCH_OWNER=Apicurio
ARG APICURIO_QE_BRANCH_VERSION=1.12.x

RUN git clone --recurse-submodules -b ${SYNDESIS_QE_BRANCH_VERSION} https://github.com/${SYNDESIS_QE_BRANCH_OWNER}/syndesis-qe.git /syndesis-qe \
	&& git clone -b ${APICURIO_QE_BRANCH_VERSION} https://github.com/${APICURIO_QE_BRANCH_OWNER}/apicurio-qe.git /apicurio \
	&& git clone https://github.com/jboss-fuse/marketplace-utilities /marketplace-utilities \
	&& cd /syndesis-qe/syndesis-extensions \
	&& git checkout 1.10.x

#================================================================================================================================
FROM maven:3.6-jdk-8-slim as buildApicurio
COPY --from=gitRepoSyndesisQe /marketplace-utilities /marketplace-utilities
COPY --from=gitRepoSyndesisQe /apicurio /apicurio

RUN mvn clean install -DskipTests=true -f /marketplace-utilities/pom.xml \
	&& mvn clean install -DskipTests=true -f /apicurio/pom.xml

#================================================================================================================================
FROM maven:3.5-jdk-8-alpine as buildSyndesisQE

## Copy built Syndesis and Apicurio dependencies
COPY --from=buildSyndesis /root/.m2/repository/io/syndesis /root/.m2/repository/io/syndesis
COPY --from=buildApicurio /root/.m2/repository/apicurio /root/.m2/repository/apicurio
COPY --from=buildApicurio /root/.m2/repository/io/syndesis/qe /root/.m2/repository/io/syndesis/qe
COPY --from=gitRepoSyndesisQe /syndesis-qe /syndesis-qe

WORKDIR /syndesis-qe

## Build syndesis extensions which are used in the test suite
RUN mvn clean package -B -f syndesis-extensions/pom.xml --also-make --projects :syndesis-library-jdbc-driver,:syndesis-extension-log-body,:syndesis-extension-body

## Build syndesis-qe tests (To download all dependencies during the building phase. not during the running phase)
RUN mvn -P ui clean install -DskipTests -Dwebdriverextensions.version=3.1.3 -Pdownload-drivers

## This file has not contained 3rd party services. It is copied into the test suite in order to the tests which use self-deployed services work (AMQ broker, MongoDB).
## In the future, the external credential.json (with 3rd party services credential) can be pass to the image as an environment variable.
COPY credentials_minimal.json /syndesis-qe/credentials.json

#================================================================================================================================
FROM selenium/standalone-firefox:78.0 as runningEnv

WORKDIR /home/seluser
COPY --from=buildSyndesisQE /root/.m2/repository/ /home/seluser/.m2/repository/
COPY --from=buildSyndesisQE /syndesis-qe /home/seluser/syndesis-qe
COPY --from=gitRepoSyndesis /fuse-online-install /home/seluser/fuse-online-install
COPY entrypoint.sh /home/seluser/entrypoint.sh
## Default environment variables values
ENV TAGS '@smoke'
ENV MODULE 'all'
ENV MODE 'full'
ENV RETRIES '3'

USER root
RUN apt update -y \
	&& apt install -y openjdk-8-jdk \
	&& wget -q https://mirror.openshift.com/pub/openshift-v4/clients/ocp/latest/openshift-client-linux.tar.gz -O /tmp/oc.tar.gz \
	&& tar -xvf /tmp/oc.tar.gz oc \
	&& mv oc /usr/local/bin \
	&& rm -f /tmp/oc.tar.gz \
	&& chown -R seluser:seluser /home/seluser

# the root cannot be used, the webdriver would fail after start
USER seluser
WORKDIR /home/seluser/syndesis-qe

ENTRYPOINT ["/home/seluser/entrypoint.sh"]

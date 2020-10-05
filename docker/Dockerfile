#================================================================================================================================
# Default version, for now, is 1.10.x, `docker build -t syndesisqe/tests-ui .`
# With the BUILD ARG, you can specify which version of Syndesis QE and Syndesis will be used in the final image
# e.g. `docker build -t docker.io/syndesisqe/tests-ui:<tag> --build-arg SYNDESIS_BRANCH_VERSION=1.10.x --build-arg SYNDESIS_QE_BRANCH_VERSION=1.10.x .`
# If you want to use your Syndesis/SyndesisQE fork during building, you can specify a build arg SYNDESIS_BRANCH_OWNER or SYNDESIS_QE_BRANCH_OWNER
# e.g. For building an image for testing changes in the code base which are committed only in the owner repo:
# `docker build -t docker.io/syndesisqe/tests-ui:<tag> --build-arg SYNDESIS_QE_BRANCH_VERSION=FUSEQE-9071_Containerizing-test-suite --build-arg SYNDESIS_QE_BRANCH_OWNER=mkralik3 .`


# You have to specify the cluster URL, NAMESPACE and ui credentials in the environment variables. When you don't specify tags, only the smoke test will be running.
# You can specify multiple test tags using cucumber notation via TAGS environment variable  like `-e TAGS='@smoke OR @database'`
# ! Be aware that the tests which use 3rd party services specified in credentials.json are not supported yet !
# The image contains only minimal credential.json in order to the tests which use self-deployed services work (AMQ broker, MongoDB).
# You have also share memory for container, otherwise the firefox crash. (`--shm-size=2g`)
## e.g.
#docker run \
#   -e URL='https://192.168.42.236:8443' \
#   -e UI_USERNAME='admin' \
#   -e UI_PASSWORD='admin' \
#   -e NAMESPACE='mkralik' \
#   --shm-size=2g \
#   syndesisqe/tests-ui:1.10.x
#
#
# For debugging, you have to expose port 5005 by `-p 5005:5005`
# For running tests with 3rd party services, use CREDENTIALS_URL env `-e CREDENTIALS_URL=http://.../credentials.json`
# If you want to see the test results (in case the test failed) after the container is finished, you have to mount any folder from
# your local machine (/tmp/syndesisQe in the example) to the container. `-v <your folder in localhost>:/test-run-results:Z`
#================================================================================================================================

FROM alpine/git as gitRepos

ARG SYNDESIS_BRANCH_OWNER=syndesisio
ARG SYNDESIS_BRANCH_VERSION=1.10.x

ARG SYNDESIS_QE_BRANCH_OWNER=syndesisio
ARG SYNDESIS_QE_BRANCH_VERSION=1.10.x

ARG APICURIO_QE_BRANCH_OWNER=Apicurio
ARG APICURIO_QE_BRANCH_VERSION=1.10.x

ARG INSTALL_TAG=1.10

RUN git clone https://github.com/${SYNDESIS_BRANCH_OWNER}/syndesis.git /syndesis \
	&& cd /syndesis \
	&& git checkout ${SYNDESIS_BRANCH_VERSION} \
	&& git clone --recurse-submodules https://github.com/${SYNDESIS_QE_BRANCH_OWNER}/syndesis-qe.git /syndesis-qe \
	&& cd /syndesis-qe \
	&& git checkout ${SYNDESIS_QE_BRANCH_VERSION} \
	&& cd /syndesis-qe/syndesis-extensions \
	&& git checkout master \
	&& git clone https://github.com/${APICURIO_QE_BRANCH_OWNER}/apicurio-qe.git /apicurio \
	&& cd /apicurio \
	&& git checkout ${APICURIO_QE_BRANCH_VERSION} \
	&& git clone https://github.com/${SYNDESIS_BRANCH_OWNER}/fuse-online-install.git /fuse-online-install \
	&& cd /fuse-online-install \
	&& git checkout ${INSTALL_TAG}

#================================================================================================================================
## The rest tests and utils modules use some Syndesis artifacts which are not available, it needs to build them and copy to the final image
# for some reason, the syndesis build doesn't work on alpine image
FROM maven:3.6-jdk-8-slim as buildSyndesis
## Syndesis build needs git for some reason
RUN apt-get update -y \
	&& apt-get install -y git
## Build only dependencies that the test suite needs
COPY --from=gitRepos /syndesis /syndesis
WORKDIR /syndesis/app
RUN mvn clean install -P flash --projects :common-model,:server-endpoint --also-make

#================================================================================================================================
FROM maven:3.6-jdk-8-slim as buildApicurio
COPY --from=gitRepos /apicurio /apicurio
WORKDIR /apicurio
RUN mvn clean install -DskipTests=true

#================================================================================================================================
FROM maven:3.6-jdk-8-slim as buildSyndesisQE

## Copy builded Syndesis and Apicurio dependencies
COPY --from=buildSyndesis /root/.m2/repository/io/syndesis /root/.m2/repository/io/syndesis
COPY --from=buildApicurio /root/.m2/repository/apicurio /root/.m2/repository/apicurio

COPY --from=gitRepos /syndesis-qe /syndesis-qe
WORKDIR /syndesis-qe

## Build syndesis extensions which are used in the test suite
# TODO build extensions failed, skip for now
#RUN mvn clean package -B -f syndesis-extensions/pom.xml --also-make --projects :syndesis-library-jdbc-driver,:syndesis-extension-log-body,:syndesis-extension-body

## Build syndesis-qe tests (To download all dependencies during the building phase. not during the running phase)
RUN mvn -pl utilities clean install \
	&& mvn clean install -DskipTests -Pdownload-drivers

## This file has not contained 3rd party services. It is copied into the test suite in order to the tests which use self-deployed services work (AMQ broker, MongoDB).
## In the future, the external credential.json (with 3rd party services credential) can be pass to the image as an environment variable.
COPY credentials_minimal.json /syndesis-qe/credentials.json

#================================================================================================================================
FROM selenium/standalone-firefox:78.0 as runningEnv

WORKDIR /home/seluser
COPY --from=buildSyndesisQE /root/.m2/repository/ /home/seluser/.m2/repository/
COPY --from=buildSyndesisQE /syndesis-qe /home/seluser/syndesis-qe
COPY --from=gitRepos /fuse-online-install /home/seluser/fuse-online-install
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

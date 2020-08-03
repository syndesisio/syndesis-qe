#================================================================================================================================
# Default version, for now, is 1.11.x, `docker build -t syndesisqe/tests-rest:1.11.x .`
# With the BUILD ARG, you can specify which version of Syndesis QE and Syndesis will be used in the final image
# e.g. `docker build -t docker.io/syndesisqe/tests-rest:<tag> --build-arg SYNDESIS_BRANCH_VERSION=1.10.x --build-arg SYNDESIS_QE_BRANCH_VERSION=1.10.x .`
# If you want to use your Syndesis/SyndesisQE fork during building, you can specify a build arg SYNDESIS_BRANCH_OWNER or SYNDESIS_QE_BRANCH_OWNER
# e.g. For building an image for testing changes in the code base which are committed only in the owner repo:
# `docker build -t docker.io/syndesisqe/tests-rest:<tag> --build-arg SYNDESIS_QE_BRANCH_VERSION=FUSEQE-9071_Containerizing-test-suite --build-arg SYNDESIS_QE_BRANCH_OWNER=mkralik3 .`


# You have to specify the cluster URL, NAMESPACE and admin credentials in the environment variables. When you don't specify tags, only the smoke test will be running.
# You can specify multiple test tags using cucumber notation via TAGS environment variable  like `-e TAGS='@smoke OR @database'`
# ! Be aware that the tests which use 3rd party services specified in credentials.json are not supported yet !
# The image contains only minimal credential.json in order to the tests which use self-deployed services work (AMQ broker, MongoDB).
## e.g.
#docker run \
#   -e URL='https://192.168.42.236:8443' \
#   -e ADMIN_USERNAME='admin' \
#   -e ADMIN_PASSWORD='admin' \
#   -e NAMESPACE='mkralik' \
#   syndesisqe/tests-rest:1.11.x
#
# For debugging, you have to expose port 5005 by `-p 5005:5005`
# For running tests with 3rd party services, use CREDENTIALS_URL env `-e CREDENTIALS_URL=http://.../credentials.json`
#================================================================================================================================

FROM alpine/git as gitRepoSyndesis

ARG SYNDESIS_BRANCH_OWNER=syndesisio
ARG SYNDESIS_BRANCH_VERSION=1.11.x

WORKDIR /syndesis
RUN git clone https://github.com/${SYNDESIS_BRANCH_OWNER}/syndesis.git /syndesis
RUN git checkout ${SYNDESIS_BRANCH_VERSION}

#================================================================================================================================
## The rest tests and utils modules use some Syndesis artifacts which are not available, it needs to build them and copy to the final image
# for some reason, the syndesis build doesn't work on alpine image
FROM maven:3.6-jdk-8-slim as buildSyndesis
## Syndesis build needs git for some reason
RUN apt-get update -y
RUN apt-get install -y git
## Build only dependencies that the test suite needs
COPY --from=gitRepoSyndesis /syndesis /syndesis
WORKDIR /syndesis/app
RUN mvn clean install -P flash --projects :common-model,:server-endpoint --also-make

#================================================================================================================================
FROM alpine/git as gitRepoSyndesisQe

ARG SYNDESIS_QE_BRANCH_OWNER=syndesisio
ARG SYNDESIS_QE_BRANCH_VERSION=1.11.x

WORKDIR /syndesis-qe
RUN git clone --recurse-submodules https://github.com/${SYNDESIS_QE_BRANCH_OWNER}/syndesis-qe.git /syndesis-qe
RUN git checkout ${SYNDESIS_QE_BRANCH_VERSION}

WORKDIR /syndesis-qe/syndesis-extensions
RUN git checkout master

#================================================================================================================================
FROM maven:3.6-jdk-8-slim as buildSyndesisQE

## Default environment variables values
ENV TAGS '@smoke'

## Copy builded Syndesis dependencies
COPY --from=buildSyndesis /root/.m2/repository/io/syndesis /root/.m2/repository/io/syndesis

COPY --from=gitRepoSyndesisQe /syndesis-qe /syndesis-qe
WORKDIR /syndesis-qe

## Build syndesis extensions which are used in the test suite
# TODO build extensions failed, skip for now
#RUN mvn clean package -B -f syndesis-extensions/pom.xml --also-make --projects :syndesis-library-jdbc-driver,:syndesis-extension-log-body,:syndesis-extension-body

## Build syndesis-qe tests (To download all dependencies during the building phase. not during the running phase)
RUN mvn -P rest clean install -DskipTests

## This file has not contained 3rd party services. It is copied into the test suite in order to the tests which use self-deployed services work (AMQ broker, MongoDB).
## In the future, the external credential.json (with 3rd party services credential) can be pass to the image as an environment variable.
COPY credentials_minimal.json /syndesis-qe/credentials.json

## the `mvn` command was moved to the script file in case the complex use case will be needed in the future
## !!!!!!! For building image for Delorean, use syndesisQe_delorean.sh entrypoint !!!!!!!
COPY syndesisQe.sh /usr/local/bin/syndesisQe.sh
#COPY syndesisQe_delorean.sh /usr/local/bin/syndesisQe.sh

# maven:3.6-jdk-8-slim image doesn't contain wget, it is needed for download credentials.json if it is provided
RUN apt-get update -y
RUN apt-get install -y wget

RUN chmod +x /usr/local/bin/syndesisQe.sh
ENTRYPOINT ["/usr/local/bin/syndesisQe.sh"]

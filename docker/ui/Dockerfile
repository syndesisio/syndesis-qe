#================================================================================================================================
# Default version, for now, is 1.11.x, `docker build -t syndesisqe/tests-ui:1.11.x .`
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
#   syndesisqe/tests-ui:1.11.x
#
#
# For debugging, you have to expose port 5005 by `-p 5005:5005`
# For running tests with 3rd party services, use CREDENTIALS_URL env `-e CREDENTIALS_URL=http://.../credentials.json`
# If you want to see the test results (in case the test failed) after the container is finished, you have to mount any folder from
# your local machine (/tmp/syndesisQe in the example) to the container. `-v <your folder in localhost>:/test-run-results:Z`
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

ARG APICURIO_QE_BRANCH_OWNER=Apicurio
ARG APICURIO_QE_BRANCH_VERSION=1.11.x

WORKDIR /syndesis-qe
RUN git clone --recurse-submodules https://github.com/${SYNDESIS_QE_BRANCH_OWNER}/syndesis-qe.git /syndesis-qe
RUN git checkout ${SYNDESIS_QE_BRANCH_VERSION}

WORKDIR /apicurio
RUN git clone https://github.com/${APICURIO_QE_BRANCH_OWNER}/apicurio-qe.git /apicurio
RUN git checkout ${APICURIO_QE_BRANCH_VERSION}

WORKDIR /syndesis-qe/syndesis-extensions
RUN git checkout master

#================================================================================================================================
# TODO skip for now, see next TODO
#FROM maven:3.6-jdk-8-slim as buildApicurio
#COPY --from=gitRepoSyndesisQe /apicurio /apicurio
#WORKDIR /apicurio
#RUN mvn clean install -DskipTests=true

#================================================================================================================================
FROM maven:3.6-jdk-8-slim as buildSyndesisQE

## Copy builded Syndesis and Apicurio dependencies
COPY --from=buildSyndesis /root/.m2/repository/io/syndesis /root/.m2/repository/io/syndesis

# TODO, once marketplace-utilities is pushed to github or central, the apicurito can be build manually, for now, fill the internal repository url bellow
#COPY --from=buildApicurio /root/.m2/repository/apicurio /root/.m2/repository/apicurio
RUN mvn org.apache.maven.plugins:maven-dependency-plugin:3.1.2:get \
    -DremoteRepositories=<! FILL HERE INTERNAL REPO URL WHERE APICURIO 0.0.3 IS !> \
    -DartifactId=apicurito-tests \
    -Dclassifier=tests \
    -DgroupId=apicurio \
    -Dversion=0.0.3-SNAPSHOT \
    -Dtype=test-jar

COPY --from=gitRepoSyndesisQe /syndesis-qe /syndesis-qe
WORKDIR /syndesis-qe

## Build syndesis extensions which are used in the test suite
# TODO build extensions failed, skip for now
#RUN mvn clean package -B -f syndesis-extensions/pom.xml --also-make --projects :syndesis-library-jdbc-driver,:syndesis-extension-log-body,:syndesis-extension-body

## Build syndesis-qe tests (To download all dependencies during the building phase. not during the running phase)
RUN mvn -P ui clean install  -DskipTests

WORKDIR /syndesis-qe/ui-common
RUN mvn clean package -Pdownload-drivers

## This file has not contained 3rd party services. It is copied into the test suite in order to the tests which use self-deployed services work (AMQ broker, MongoDB).
## In the future, the external credential.json (with 3rd party services credential) can be pass to the image as an environment variable.
COPY credentials_minimal.json /syndesis-qe/credentials.json

#================================================================================================================================
FROM selenium/standalone-firefox:78.0 as runningEnv

WORKDIR /syndesis-qe
COPY --from=buildSyndesisQE /root/.m2/repository/ /home/seluser/.m2/repository/
COPY --from=buildSyndesisQE /syndesis-qe /syndesis-qe
## Default environment variables values
ENV TAGS '@smoke'

USER root
RUN apt update -y
RUN apt install -y openjdk-8-jdk
# the root cannot be used, the webdriver would fail after start
USER seluser

COPY syndesisQe_ui.sh /usr/local/bin/syndesisQe.sh
RUN chmod +x /usr/local/bin/syndesisQe.sh

ENTRYPOINT ["/usr/local/bin/syndesisQe.sh"]

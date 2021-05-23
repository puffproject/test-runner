FROM openjdk:8-jre-alpine
# FROM openjdk:11-jre
# FROM openjdk:14-alpine

# TODO ADD OTHER INSTALLED PACKAGES/DEPENDENCIES

ARG DIR

WORKDIR $DIR
USER root

ADD . .
RUN chmod a+x java_entry.sh

ENTRYPOINT ["sh", "./java_entry.sh"]
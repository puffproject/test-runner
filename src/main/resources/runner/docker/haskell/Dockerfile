FROM haskell:8
# TODO FIND A SMALLER BASE IMAGE TO RUN ON ALPINE
# TODO ADD ANY OTHER DEPENDENCIES

ARG DIR
WORKDIR $DIR
USER root

ADD . .
RUN chmod a+x haskell_entry.sh

ENTRYPOINT ["sh", "haskell_entry.sh"]

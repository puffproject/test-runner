FROM haskell:8

ARG DIR
ARG USER=appuser
WORKDIR $DIR
RUN adduser $USER

RUN chown -R $USER $DIR

USER $USER

RUN stack install HUnit

ADD . .
# This build the project and run the tests
ENTRYPOINT ["/bin/bash", "./haskell_entry.sh"]

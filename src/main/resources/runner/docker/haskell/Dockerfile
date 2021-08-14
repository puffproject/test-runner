FROM haskell:8

ARG DIR
ARG USER=jeff # Consider making this an env variable
WORKDIR $DIR
RUN adduser $USER

RUN chown -R $USER /code

USER $USER

RUN stack install HUnit

ADD . .
# This build the project and run the tests
ENTRYPOINT ["sh", "./haskell_entry.sh"]

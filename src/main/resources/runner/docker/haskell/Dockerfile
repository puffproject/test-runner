FROM haskell:8
RUN stack install HUnit

ARG DIR
ARG USER=jeff # Consider making this an env variable
WORKDIR $DIR
RUN adduser $USER

## `haskell_entry.sh` will be at `/code/haskell_entry.sh`

USER $USER
ADD . .
# This build the project and run the tests
ENTRYPOINT ["sh", "hakell_entry.sh"]

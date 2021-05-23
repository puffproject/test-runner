FROM python:3.6-alpine
RUN pip install pytest

ARG DIR

WORKDIR $DIR
USER root

ADD . .
RUN chmod a+x python_entry.sh

ENTRYPOINT ["sh", "./python_entry.sh"]
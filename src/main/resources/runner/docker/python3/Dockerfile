FROM python:3.6-alpine
RUN pip install pytest

ARG DIR
WORKDIR $DIR

RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

ADD . .

ENTRYPOINT ["sh", "./python_entry.sh"]
FROM jfloff/alpine-python:3.6

RUN pip install --upgrade \
	Flask \
	pip

COPY app /app

WORKDIR app
ENV FLASK_APP=hello.py
EXPOSE 5000

ENTRYPOINT ["flask", "run", "--host", "0.0.0.0"]

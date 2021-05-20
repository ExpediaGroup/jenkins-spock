.PHONY: all
all:
	$(MAKE) test
	$(MAKE) test-pipeline

.PHONY: build
build:
	docker build -t helper-script .

.PHONY: run
run: build
	docker run --rm -it --publish 5000:5000 helper-script

.PHONY: test
test: build
	docker run --entrypoint python helper-script -m unittest discover

.PHONY: test-pipeline
test-pipeline:
	./gradlew clean build

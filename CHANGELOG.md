Changelog
==============================

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](http://keepachangelog.com/en/1.0.0/) and this project adheres to [Semantic Versioning](http://semver.org/spec/v2.0.0.html).

## Unreleased

* Make the resource path lookup customizable for `JenkinsPipelineSpecification#loadPipelineScriptForTest`. To define your own path set `JenkinsPipelineSpecification.scriptClassPath`.

### Fixed

* Added `<repositories>` section to all poms pointing to the Jenkins Release repository - so that Jenkins artifacts can be successfully located during builds.

## 2.0.1

_Release Date: 2018-09-13_

### Fixed

* Layout of documentation files in `-javadoc` artifact was incorrect.

## 2.0.0

_Release Date: 2018-09-12_

Initial OSS Release.

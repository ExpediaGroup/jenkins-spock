Changelog
==============================

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](http://keepachangelog.com/en/1.0.0/) and this project adheres to [Semantic Versioning](http://semver.org/spec/v2.0.0.html).

## Unreleased

* Fixed a bug for Java 11 compatibility

## 2.1.4

_Release Date: 2020-06-01_

### Added

* Solution for Jenkins extensions that interact with the `Jenkins` singelton at classload- or Descriptor-instantiation-time:
	* jenkins-spock automatically injects a mock Jenkins singleton _before_ any Jenkins extensions are classloaded or instantiated, so that `Jenkins.getInstanceOrNull()` is not `null`.
	* `makeStaticJenkins()` method to allow test suites to provide their own pre-test-suite Jenkins singleton if necessary, such as if the spec needs to stub pre-test-suite interaction with `Jenkins`
	* Please see the "Mock Jenkins" section of the `JenkinsPipelineSpecification` GroovyDoc.

## 2.1.3

_Release Date: 2020-05-29_

### Updated

* `jenkins-spock` version used in examples is updated to the released version on release.

## 2.1.2

_Release Date: 2020-03-12_

### Updated

* Updated Reflections to 0.9.12

## 2.1.1

_Release Date: 2020-02-06_

### Fixed

* Could not use `loadPipelineScriptForTest(...)` with Groovy scripts whose filenames were not valid Java class names.

## 2.1.0

_Release Date: 2020-02-04_

### Fixed

* Added `<repositories>` section to all poms pointing to the Jenkins Release repository - so that Jenkins artifacts can be successfully located during builds.

### Updated
	
* Emit more-specific exception when tests call a Groovy method with an incorrect signature.
* Clarified documentation for using "global" variables in tests.

### Added

* Make the resource path lookup customizable for `JenkinsPipelineSpecification#loadPipelineScriptForTest`. To define your own path set `JenkinsPipelineSpecification.scriptClassPath`.
* The `maven-invoker-plugin` now runs some of the "working example" projects as integration tests.
* The `parallel()` pipeline step is special-cased and will execute all of its closures during tests.

## 2.0.1

_Release Date: 2018-09-13_

### Fixed

* Layout of documentation files in `-javadoc` artifact was incorrect.

## 2.0.0

_Release Date: 2018-09-12_

Initial OSS Release.

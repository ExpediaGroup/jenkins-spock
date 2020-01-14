Jenkins Pipeline Support for Spock
==============================

Utility classes to help with testing Jenkins pipeline scripts and functions written in Groovy.

User Guide _([GroovyDoc](https://javadoc.io/doc/com.homeaway.devtools.jenkins/jenkins-spock))_
==============================

Add this library to `pom.xml` in the `test` scope:

```xml
<dependency>
	<groupId>com.homeaway.devtools.jenkins</groupId>
	<artifactId>jenkins-spock</artifactId>
	<scope>test</scope>
</dependency>
```

Check the [CHANGELOG.md](CHANGELOG.md) to find the available versions.

Working Examples
-------------------------

The [`examples` directory](examples) contains working sample projects that show off the major kinds of project this library can be used with.
Check them out and try building them yourself!

Specifications
-------------------------

This library provides a `JenkinsPipelineSpecification` class that extends the Spock testing framework's `Specification` class. To test Jenkins pipeline Groovy code, extend `JenkinsPipelineSpecification` instead of `Specification`.
Please see the [GroovyDoc for `JenkinsPipelineSpecification`](http://javadoc.io/page/com.homeaway.devtools.jenkins/jenkins-spock/latest/com/homeaway/devtools/jenkins/testing/JenkinsPipelineSpecification.html) for specific usage information and the [Spock Framework Documentation](http://docs.spockframework.org/) for general usage information.

During the tests of a `JenkinsPipelineSpecification` suite,

1. All Jenkins pipeline steps (`@StepDescriptor`s) will be globally callable, e.g. you can just write `sh( "echo hello" )` anywhere.
	1. "Body" closure blocks passed to any mock pipeline steps will be executed.
2. All Jenkins pipeline variables (`@Symbol`s and `GlobalVariable`s) will be globally accessible, e.g. you can just write `docker.inside(...)` anywhere
3. All Pipeline Shared Library Global Variables (from the `/vars` directory) will be globally accessible, so you can just use them anywhere.
4. All interactions with any of those pipeline extension points will be captured by Spock mock objects.
5. You can load any Groovy script (`Jenkinsfile` or Shared Library variable) to unit-test it in isolation.
6. The `Jenkins` singleton instance will exist as a Spock mock object.
7. The `CpsScript` execution will exist as a Spock spy object (you should never need to interact with this, but it's there).

Dependencies
-------------------------

There are some dependencies of this library that are marked with Maven's `provided` scope.
This means that Maven will pull them in for building and testing _this library_, but when you use this library you must pull those libraries in as dependencies yourself.

This is done because these dependencies - things like the Jenkins Pipeline API, JUnit, etc - are things that

1. You absolutely have to have as dependencies in your project in order for this library to be of any use
2. Should not have their version or final scope controlled by this library

The dependencies that should already be in your project in order for using this library to make any sense are:

```xml
<dependency>
	<groupId>org.jenkins-ci.main</groupId>
	<artifactId>jenkins-core</artifactId>
	<version>${jenkins.version}</version>
	<scope>test</scope>
</dependency>
<dependency>
	<groupId>org.jenkins-ci.plugins.workflow</groupId>
	<artifactId>workflow-step-api</artifactId>
	<version>${jenkins.workflow.step.version}</version>
	<scope>test</scope>
</dependency>
<dependency>
	<groupId>org.jenkins-ci.plugins.workflow</groupId>
	<artifactId>workflow-cps</artifactId>
	<version>${jenkins.workflow.cps.version}</version>
	<scope>test</scope>
</dependency>
<dependency>
	<groupId>org.jenkins-ci</groupId>
	<artifactId>symbol-annotation</artifactId>
	<version>${jenkins.symbol.version}</version>
	<scope>test</scope>
</dependency>

<dependency>
	<groupId>junit</groupId>
	<artifactId>junit</artifactId>
	<version>${junit.version}</version>
	<scope>test</scope>
</dependency>

<dependency>
	<groupId>javax.servlet</groupId>
	<artifactId>javax.servlet-api</artifactId>
	<version>${jenkins.servlet.version}</version>
	<scope>test</scope>
</dependency>
```

Depending on your parent pom, some of the `${jenkins.version}` properties may already be defined. Be sure you define any that are not.

If your code actually writes code against classes in any of these dependencies, remove the `<scope>test</scope>` entry for the corresponding block(s).

Developer Guide
==============================

Building
-------------------------

The build of **jenkins-spock** is built with Maven. Normal [Maven lifecycle phases](https://maven.apache.org/guides/introduction/introduction-to-the-lifecycle.html) apply.
As long as you have a contemporary (1.8+) JDK and Maven (3.3+), it should build fine.

Testing
-------------------------

Unit tests of `jenkins-spock` will happen automatically during the `test` phase of the Maven build.

There is an `it` [Maven Profile]() that can be activated to run _integration tests_:

	mvn verify -Pit

The integration tests will try to test each project in the [src/test/resources/test-projects](src/test/resources/test-projects) using the current `jenkins-spock` code.

Releasing
-------------------------

**jenkins-spock** should be released by the [`maven-release-plugin`](https://maven.apache.org/maven-release/maven-release-plugin/):

	mvn clean release:prepare release:perform

In order for this to succeed, the user running this must

1. Configure GitHub credentials with `push` access to this repository.
2. Configure Sonatype Nexus credentials with deploy access to the `com.homeaway` groupId.
3. Configure a PGP identity so that the [`maven-gpg-plugin`](https://maven.apache.org/plugins/maven-gpg-plugin/) can sign artifacts.

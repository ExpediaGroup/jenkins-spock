Pipeline Shared Library
==============================

This project is a Jenkins Pipeline Shared Library that could be loaded by the [Pipeline Shared Groovy Libraries Plugin](https://plugins.jenkins.io/workflow-cps-global-lib).

It delivers two new pipeline steps:

`DefaultPipeline`
-------------------------

A simple Jenkins pipeline that 

1. Builds & Tests all branches
2. Notifies Slack if tests fail
3. Deploys the "master" branch to the Production environment
4. Deploys other branches to the "Test" environment.

Usage:

`Jenkinsfile`:

```groovy
@Library("shared-library") _
DefaultPipeline()
```

`Deployer`
-------------------------

A step that SSHs to a machine in an environment, and bounces an application using `docker-compose`.

Usage:

```
Deployer("test") // deploy the latest version of the application to the TEST environment
```

Testing
==============================

To test the library, run `./gradlew clean build --info`.

Requirements
==============================

The following tools should be installed in order to work with this project:

1. `make`
2. `java` 1.8+

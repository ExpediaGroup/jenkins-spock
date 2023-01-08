Jenkinsfile
==============================

This project is a containerized Python web application with a Jenkins pipeline that

1. Builds & Tests all branches
2. Notifies Slack if tests fail
3. Deploys the "master" branch to the Production environment
4. Deploys other branches to the "Test" environment.

Building
==============================

To build the web application, run `make build`.

Running
==============================

To run the application, run `make run`.

To see the running application, visit http://localhost:5000 in a web browser.

Testing
==============================

To test the _application_, run `make test`.

To test the _pipeline_, run `make test-pipeline`.

Requirements
==============================

The following tools should be installed in order to work with this project:

1. `docker`
2. `make`
3. `java` 1.8+

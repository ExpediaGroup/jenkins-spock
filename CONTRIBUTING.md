Contribution Guide
==============================

Bug Reports & Feature Requests
-------------------------

If you have a bug report or feature request that is not accompanied by a Pull Request, please create a GitHub Issue.

Code Changes
-------------------------

To submit code changes, please fork this repository and send a pull request to the `master` branch.

### Code Style

Please make an effort to match the style present in the particular files that you edit.

### Testing

If code changes are testable, the pull request should include new test code.

### Documentation

Any code change that changes the behavior of the project should be accompanied by appropriate documentation updates.
This includes updating the "unreleased" section of the [`CHANGELOG`](CHANGELOG.md).

Documentation Changes
-------------------------

_(such as to README and other `*.md` files)_

To submit changes to _documentation_ (JavaDoc/GroovyDoc changes are _code changes_ as these changes are made in source code files), please fork this repository and send a pull request to the `master` branch.

Project maintainers may make documentation-only commits directly to the `master` branch without going through the Pull Request process.

Documentation-only commits do not require an update to the [`CHANGELOG`](CHANGELOG.md).

CI/CD
-------------------------

There is currently no CI/CD system connected. HomeAway's maintainers will manually verify that pull request code can build & passes its tests.

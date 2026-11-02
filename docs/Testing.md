---
  layout: default.md
  title: "Testing guide"
  pageNav: 3
---

# Testing guide

<!-- * Table of Contents -->
<page-nav-print />

<!-- -------------------------------------------------------------------------------------------------------------------- -->

## Running tests

You can run tests in two ways.

* **Method 1: Using IntelliJ JUnit test runner**
  * To run all tests, choose the shared `All tests` run configuration, which has assertions enabled (VM option `-ea`)
  * To run a subset of tests, you can right-click on a test package,
    test class, or a test and choose `Run 'ABC'`
* **Method 2: Using Gradle**
  * Open a console and run the command `gradlew clean test` (Mac/Linux: `./gradlew clean test`)
  * Gradle runs the tests, and `gradlew run` runs the app, with assertions enabled

<box type="info" seamless>

**Link**: Read [this Gradle Tutorial from the se-edu/guides](https://se-education.org/guides/tutorials/gradle.html) to learn more about using Gradle.
</box>

--------------------------------------------------------------------------------------------------------------------

## Running the checks that CI runs

`gradlew clean test` runs only the tests. CI runs `gradlew check coverage` (Mac/Linux: `./gradlew check coverage`),
which also runs Checkstyle and creates a coverage report:

* The coverage report is in `build/reports/jacoco/coverage/`: open `html/index.html` in a browser, or read
  `coverage.xml`.
* The GUI tests need a display. Without one, e.g., on a Linux machine with no desktop, they are skipped instead of
  failing. To run them there, install Xvfb and use `xvfb-run -a ./gradlew check coverage`, as CI does on Linux.
* To see whether any test was skipped, open the test report, `build/reports/tests/test/index.html`.
* CI also runs repository-wide checks, such as line endings. See
  [Repository-wide checks](DevOps.md#repository-wide-checks) in the DevOps guide.

--------------------------------------------------------------------------------------------------------------------

## Types of tests

This project has three types of tests:

1. *Unit tests* target the lowest-level methods and classes.<br>
   For example: `seedu.address.commons.util.StringUtilTest`
1. *Integration tests* check how multiple code units work together; the individual units are assumed to work.<br>
   For example: `seedu.address.storage.StorageManagerTest`
1. *Hybrid tests* combine unit and integration testing. These tests check both the individual units and how they work together.<br>
   For example: `seedu.address.logic.LogicManagerTest`

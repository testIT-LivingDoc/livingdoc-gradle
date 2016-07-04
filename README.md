# livingdoc-gradle
LivingDoc Plugin for Gradle

[![Build Status](https://travis-ci.org/nachevn/livingdoc-gradle.svg?branch=master)](https://travis-ci.org/nachevn/livingdoc-gradle)

This plugin enables you to freeze and run any LivingDoc specifications as part of your build.

## Use the Plugin
Currently, there is no jar file available from this project. In order to use the plugin you need to clone this repository and install the project artifact locally.

Install the project artifact:

    ./gradlew install

As a result of the install task a new livingdoc-gradle-${VERSION}.jar is created which can be found in the local maven repository. In order to use the plugin you need an appropriate buildscript configuration:

    buildscript {
      repositories {
        jcenter()
        mavenLocal() // used to get the latest version of the install livingdoc-gradle plugin
      }

      dependencies {
        classpath "info.novatec.testit:livingdoc-gradle:+" // get the plugin into the classpath
      }
    }

    apply plugin: 'livingdoc'

    livingdoc {
      uiTest { // a random name, in order to identify the used sourceSet of resources
        fixtureSourceDirectory = file('path/to/the/fixtures/java') // the source directory of your fixtures
        resources { // is optional
          // there can be one or more project resources
          uiTestResources {
            directory = file('path/to/the/fixtures/resources') // directory for specific resources
          }
        }
        specsDirectory = file('build/livingdoc/specs') // the download directory of the LivingDoc specifications
        systemUnderDevelopment = "fully qualified name of your SystemUnderDevelopment class"
        reportsType = "xml" // optional parameter
        debug = true // optional parameter
        sud = "sud_name"
        repositories {
          // there can be one or more repositories
          nameOfRepository { // a random name
            respositoryImplementation = "fully qualified name of your suit resolver class, for instance:
            info.novatec.testit.livingdoc.repository.FileSystemRepository"
            repositoryURL = "${URL_ADDRESS}"
            repositoryUID = "${REPOSITORY_UID}"
          }
        }
      }
    }

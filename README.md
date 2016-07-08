# livingdoc-gradle
LivingDoc Plugin for Gradle

[![Build Status](https://travis-ci.org/nachevn/livingdoc-gradle.svg?branch=master)](https://travis-ci.org/nachevn/livingdoc-gradle)

This plugin enables you to freeze and run any LivingDoc specifications as part of your build.

## How to use the plugin
Currently, there is no official Gradle plugin available. In order to use the plugin you need to clone this repository and install the project artifact locally.

Clone project:
    git clone [latest stable version](https://github.com/testIT-LivingDoc/livingdoc-gradle.git)

To install the project artifact locally run the install task within the project directory:

    ./gradlew install

As a result of the install task a new livingdoc-gradle-${LIVINGDOC_GRADLE_VERSION}.jar is created and can be found in the local maven repository.

## Apply the plugin to your project
In order to use the plugin you need an appropriate buildscript configuration:

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

## Available default configurations
As soon as the plugin is applied a livingdoc extension block is created

    livingdoc {
      ...
    }

as well as both default dependency configurations:

    dependencies {
      livingdocCompile ...
      livingdocRuntime ...
    }

## Create an appropriate configuration
In order to use this plugin an appropriate configuration is needed e.g.:

    livingdoc {
      uiTest {
        fixtureSourceDirectory = file('path/to/the/fixtures/java')
        resources {
          uiTestResources {
            directory = file('path/to/the/fixtures/resources')
          }
        }
        specsDirectory = file('build/livingdoc/specs')
        systemUnderDevelopment = "info.novatec.testit.livingdoc.SystemUnderDevelopment"
        reportsType = "xml"
        debug = true
        sud = "sud_name"
      }
      repositories {
        confluenceRepository {
          implementation = "info.novatec.testit.livingdoc.repository.FileSystemRepository"
          url = "http://localhost:1990/confluence/rpc/xmlrpc?handler=livingdoc1&sut=Demo&includeStyle=true&implemented=true#LIVINGDOCDEMO"
          uid = "Confluence-LIVINGDOCDEMO"
          freezedir = file('build/livingdoc/specs')
          specsfilter = /^.* My Special UITEST/
        }
      }
    }

## Available plugin functionality
There are lot's of options that can be used to configure the plugin.

### livingdoc extension block
The livingdoc extension block can contains two different types of configurations a repositories configuration as well as one or more sourceSet type configurations.

The repositories configuration closure is used to define one or more repositories which are used by freeze of the specifications e.g.

    repositories {

      // a random name
      repository1 {

        // fully qualified name of your suit resolver class
        implementation = "info.novatec.testit.livingdoc.repository.FileSystemRepository"

        // the url to your confluence repository
        url = "${URL_ADDRESS}"

        // the repository uid
        uid = "${REPOSITORY_UID}"

        // the local directory used to freeze the specifications
        freezedir = file("/path/to/freeze/directory")

        // optional, if set the filter will be applied to the set of specifications that will be freezed
        specsfilter = /.*/

      }

    }

The sourceSet type configuration represents a group of Java source, resources and their configurations e.g.:

    // a random name, in order to identify the used sourceSet of resources
    uiTest {

      // the source directory of your fixtures
      fixtureSourceDirectory = file('path/to/the/fixtures/java')

      // optional, it can be used to specify resources that are used
      resources {

        // there can be one or more project resources
        uiTestResources {

          // directory for specific resources
          directory = file('path/to/the/fixtures/resources')

        }

      }

      // the download directory of the LivingDoc specifications, notice that this path represents the directory where the specifications are freezed
      specsDirectory = file('build/livingdoc/specs')

      //fully qualified name of your SystemUnderDevelopment class
      systemUnderDevelopment = "info.novatec.testit.livingdoc.systemunderdevelopment.DefaultSystemUnderDevelopment"

      // optional parameter, if empty the default html type is used
      reportsType = "xml"

      // optional parameter, default false
      debug = true

      sud = "sud_name"

      // optional, in case that additional run arguments are needed
      additionalRunArgs

      // optional, in case that additional classpath arguments are needed
      additionalRunClasspath

    }

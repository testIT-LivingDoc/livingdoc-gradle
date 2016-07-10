# livingdoc-gradle
LivingDoc Plugin for Gradle

[![Build Status](https://travis-ci.org/nachevn/livingdoc-gradle.svg?branch=master)](https://travis-ci.org/nachevn/livingdoc-gradle)

This plugin enables you to freeze and run any LivingDoc specifications as part of your build.

## How to use the plugin
Currently, there is no official Gradle plugin available. In order to use this plugin you need to clone the repository and install the project artifact locally.

Clone project:

    git clone [latest stable version](https://github.com/testIT-LivingDoc/livingdoc-gradle.git)

To install the project artifact locally run the install task within the project directory:

    ./gradlew install

As a result of the install task new livingdoc-gradle-${LIVINGDOC_GRADLE_VERSION}.jar is created. The created jar can be found in the local maven repository.

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
As soon as the plugin is applied a livingdoc extension block is available

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
      fixtures {
        uiTest {
          fixtureSourceDirectory = file('path/to/the/fixtures/java')
          resources {
            uiTestResources {
              directory = file('path/to/the/fixtures/resources')
            }
          }
          systemUnderDevelopment = "info.novatec.testit.livingdoc.SystemUnderDevelopment"
          reportsType = "xml"
          debug = true
          sud = "sud_name"
        }
      }
      repositories {
        confluenceRepository {
          implementation = "info.novatec.testit.livingdoc.repository.FileSystemRepository"
          url = "http://localhost:1990/confluence/rpc/xmlrpc?handler=livingdoc1&sut=Demo&includeStyle=true&implemented=true#LIVINGDOCDEMO"
          uid = "Confluence-LIVINGDOCDEMO"
          freezeDirectory = file("${buildDir.path}/livingdoc/Daimler Xentry Portal Confluence-LDXC")
          sortfilter {
            uiTests {
              path = "uiTests"
              filter = "*UITEST.html"
            }
          }
        }
      }
    }

## Available plugin functionality
There are lots of options that can be used to customize the plugin configuration. A list of all available plugin configuration options can be found below.

### Available options within the livingdoc extension block
The livingdoc extension block can contains two different types of configurations a repositories configuration as well as a fixture configuration.

#### The __repositories__ configuration closure is used to define one or more repositories which are used by freeze of the specifications e.g.

    repositories {

      // a random name
      repository1 {

        // fully qualified name of your suit resolver class
        implementation = "info.novatec.testit.livingdoc.repository.FileSystemRepository"

        // the url to your confluence repository
        url = "${URL_ADDRESS}"

        // the repository uid
        uid = "${REPOSITORY_UID}"

        // optional, the local directory used to freeze the specifications
        freezeDirectory = file("${project.buildDir.path}/livingdoc/specs") // default

        // optional, can contains one or more filter configurations. If set the filter will be applied to the set of specifications that will be freezed
        sortfilter {

          // a random name
          filterIntTests {

            // the path where the specifications will be copied after the filter is applied
            path = "INTTESTS"

            // corresponds to the gradle copy task [includes](https://docs.gradle.org/current/dsl/org.gradle.api.tasks.Copy.html#org.gradle.api.tasks.Copy:includes)
            filter = "*INTTEST.html"

          }

          // a random name
          filterUiTests {

            // the path where the specifications will be copied after the filter is applied
            path = "UITESTS"

            // corresponds to the gradle copy task [includes](https://docs.gradle.org/current/dsl/org.gradle.api.tasks.Copy.html#org.gradle.api.tasks.Copy:includes)
            filter = "*UITEST.html"

          }

        }

      }

    }

#### The __fixtures__ configuration can contains one or more fixture configurations and represents a group of Java sources, resources and their configurations e.g.:

    fixtures {

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

    }

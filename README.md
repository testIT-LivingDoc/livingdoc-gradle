# livingdoc-gradle
LivingDoc Plugin for Gradle

[![Build Status](https://travis-ci.org/nachevn/livingdoc-gradle.svg?branch=master)](https://travis-ci.org/nachevn/livingdoc-gradle)

This plugin enables you to freeze and run any livingdoc specifications as part of your build.

## Use the Plugin
Currently, there is still no jar file available from this project. In order to use the plugin you need to clone this repository and build the project locally. As a result of the build a new livingdoc-gradle-${VERSION}.jar is created which can be found under ${project.buildDir}/libs/.

Setup the plugin like this:

    buildscript {
    	repositories {
    	  	mavenCentral()
    	}

    	dependencies {
    		classpath "info.novatec.testit:livingdoc-core:${LIVINDDOC_VERSION}@jar"
    		classpath "info.novatec.testit:livingdoc-server:${LIVINDDOC_VERSION}@jar"
    		classpath "info.novatec.testit:livingdoc-client:${LIVINDDOC_VERSION}@jar"
    		classpath "xmlrpc:xmlrpc:2.0.1"
    		classpath "org.apache.commons:commons-lang3:3.4"
    		classpath "commons-codec:commons-codec:1.9"
    		classpath "commons-io:commons-io:2.4"
    		classpath files('${PATH_TO_THIS_PROJECT}/livingdoc-gradle/build/libs/livingdoc-gradle-${LIVINDDOC_GRADLE_VERSION}.jar') // Linux
    		//classpath files('%PATH_TO_THIS_PROJECT%/livingdoc-gradle/build/libs/livingdoc-gradle-${LIVINDDOC_GRADLE_VERSION}.jar') // Windows
    	}
    }

    apply plugin: 'livingdoc'

    livingdoc {
      uiTest {
        fixtureSourceDirectory = file('path/to/the/fixtures/java') // source directory of your fixtures
    		resources {
          // there can be one or more project resources
    			projectResources {
    				directory = file('path/to/the/fixtures/resources') // directory for a specific resources
    			}
    		}
    		specsDirectory = file('build/livingdoc/specs')
    	  systemUnderDevelopment = "fully qualified name of your SystemUnderDevelopment class"
    	  reportsType = "xml" // optional parameter
    	  debug = true // optional parameter
    	  sud = "sud_name"
        repositories {
          // there can be one or more repositories
          nameOfRepository {
            respositoryImplementation = "fully qualified name of your suit resolver class, for instance: info.novatec.testit.livingdoc.repository.FileSystemRepository"
            repositoryURL = "${URL_ADDRESS}"
            repositoryUID = "${REPOSITORY_UID}"
          }
        }
      }
    }

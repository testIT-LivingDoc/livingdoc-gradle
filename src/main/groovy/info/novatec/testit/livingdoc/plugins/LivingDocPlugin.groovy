package info.novatec.testit.livingdoc.plugins

import info.novatec.testit.livingdoc.conventions.LivingDocPluginConvention
import info.novatec.testit.livingdoc.dsl.LivingDocExtension;
import info.novatec.testit.livingdoc.tasks.FreezeTask
import info.novatec.testit.livingdoc.tasks.RunLivingDocSpecsTask;

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.logging.Logger;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.NamedDomainObjectContainer

import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging

class LivingDocPlugin implements Plugin<Project> {

  private Project project

  private RunLivingDocSpecsTask runLivingDocSpecsTask

  NamedDomainObjectContainer<LivingDocExtension> livindDocExtContainer

  Logger logger = Logging.getLogger(LivingDocPlugin.class)

  @Override
  public void apply(Project project) {
    this.project = project
    this.project.convention.plugins.livingDoc = new LivingDocPluginConvention()
    this.livindDocExtContainer = this.project.container(LivingDocExtension)
    this.project.extensions.add( this.project.LIVINGDOC_SOURCESET_NAME, livindDocExtContainer)

    this.livindDocExtContainer.whenObjectAdded { LivingDocExtension livingdocExtension ->
      SourceSet extensionSourceSet = this.createExtensionSourceSet(livingdocExtension.name, livingdocExtension)
      this.project.afterEvaluate {
        this.configureSourceSet(livingdocExtension, extensionSourceSet)
        Jar compileSourceSetSourcesTask = this.createCompileFixturesTask(livingdocExtension, extensionSourceSet)
        this.createRunTasks(compileSourceSetSourcesTask, livingdocExtension, extensionSourceSet)
      }
    }
  }

  /**
   * This method is executed as soon as a LivingDocExtention configuration is created
   */
  private SourceSet createExtensionSourceSet(String extensionName, LivingDocExtension extension) {
    SourceSet extensionSourceSet = this.project.sourceSets.create(extensionName)
    this.project.configurations.getByName(extensionSourceSet.getCompileConfigurationName()){ transitive = false }
    this.project.configurations.getByName(extensionSourceSet.getRuntimeConfigurationName()){ transitive = false }
    logger.info("Configuration with name ${} created!!!", extensionSourceSet.getCompileConfigurationName())
    logger.info("Configuration with name ${} created!!!", extensionSourceSet.getRuntimeConfigurationName())

    this.project.plugins.withType(JavaPlugin) {
      this.project.configure(extensionSourceSet) {
        compileClasspath += this.project.sourceSets.getByName('main').output
        runtimeClasspath += compileClasspath
      }

      this.project.plugins.withType(org.gradle.plugins.ide.eclipse.EclipsePlugin) {
        this.project.eclipse {
          classpath {
            plusConfigurations.add(this.project.configurations.getByName(extensionSourceSet.getCompileConfigurationName()))
            plusConfigurations.add(this.project.configurations.getByName(extensionSourceSet.getRuntimeConfigurationName()))
          }
        }
      }
    }
    return extensionSourceSet
  }

  /**
   * This method is executed after the Gradle file of the project is fully initialized
   */
  private configureSourceSet(LivingDocExtension livingdocExtension, SourceSet extensionSourceSet) {
    this.project.configure(extensionSourceSet) {
      logger.info("Configure sourceSet {}", extensionSourceSet.name)
      logger.info("{} fixtureSourceDirectory is {}", extensionSourceSet.name, livingdocExtension.fixtureSourceDirectory?.path)
      logger.info("{} resources direcory is {}", extensionSourceSet.name,livingdocExtension.resources?.path)
      java.srcDirs this.project.file(livingdocExtension.fixtureSourceDirectory?.path)
      if (livingdocExtension.resources) {
        resources.srcDirs this.project.file(livingdocExtension.resources?.path)
      }
    }
  }
  /**
   * This method creates the jar file from the livingdocExtention fixtureSourceDirectory path
   * 
   * @param livingdocExtension
   * @return
   */
  private Jar createCompileFixturesTask(LivingDocExtension livingdocExtension, SourceSet extensionSourceSet) {
    Jar compileFixturesTask = this.project.tasks.create("compile${livingdocExtension.name.capitalize()}Jar", Jar)
    this.project.configure(compileFixturesTask){
      group this.project.LIVINGDOC_TASKS_GROUP
      description "Compile the ${livingdocExtension.name} classes of the ${this.project} to a jar file"
      classifier = livingdocExtension.name
      version = this.project.version
      from extensionSourceSet.output
      destinationDir this.project.file("${project.buildDir}/${livingdocExtension.name}")
    }
    return compileFixturesTask
  }

  private void createFreezeTask(LivingDocExtension livingdocExtension, SourceSet extensionSourceSet) {
    FreezeTask freezeTask = this.project.tasks.create("freeze${livingdocExtension.name}", FreezeTask)
    this.project.configure(freezeTask) {
      group this.project.LIVINGDOC_TASKS_GROUP
      description "Freezes the LivingDoc specifications of ${livingdocExtension.name}"
      repositoryUrl livingdocExtension.repositoryURL
      repositoryUid livingdocExtension.repositoryUID
      specsDirectory livingdocExtension.specsDirectory.path
      repositoryImplementation livingdocExtension.respositoryImplementation
    }
  }

  /**
   * Creates a run task per LivingDocExtension configuration
   */
  private createRunTasks(Jar compileFixturesTask, LivingDocExtension livingdocExtension, SourceSet extensionSourceSet) {
    def runSpecsArgs = [
      livingdocExtension.livingDocRunner,
      '-f',
      livingdocExtension.sudImplementation + ';' + livingdocExtension.sud,
     
    ]
    if (livingdocExtension.debug) {
     runSpecsArgs += [ '--debug' ]
    }
    if (livingdocExtension.reportsType) {
     runSpecsArgs += [ '--' + livingdocExtension.reportsType ]
    }
    runSpecsArgs += [ '-s', livingdocExtension.specsDirectory?.path ]
    
    if (livingdocExtension.reportsDirectory) {
     runSpecsArgs += ['-o', livingdocExtension.reportsDirectory.path]
    } else {
    
    runSpecsArgs += ['-o', project.buildDir.path + '/livingdoc/reports' ]
    }
    
    RunLivingDocSpecsTask livingDocRunTask = project.tasks.create("${livingdocExtension.name}Run", RunLivingDocSpecsTask)
    this.project.configure(livingDocRunTask) {
      group this.project.LIVINGDOC_TASKS_GROUP
      description "Run ${livingdocExtension.name} specifications from directory ${livingdocExtension.specsDirectory.path} on the ${this.project}"
      workingDir this.project.projectDir
      classPath compileFixturesTask.archivePath.path + File.pathSeparator + extensionSourceSet.runtimeClasspath.asPath
      procArgs += runSpecsArgs
      showOutput true
    }
  }

  private boolean checkldExtPrerequisite(LivingDocExtension extension) {
    println "Check if ${extension.fixtureSourceDirectory.path} exists: " + extension.fixtureSourceDirectory.exists()
    return extension.fixtureSourceDirectory.exists()
  }
}
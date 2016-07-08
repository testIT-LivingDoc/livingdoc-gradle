package info.novatec.testit.livingdoc.plugins

import info.novatec.testit.livingdoc.conventions.LivingDocPluginConvention
import info.novatec.testit.livingdoc.dsl.LivingDocDsl
import info.novatec.testit.livingdoc.dsl.LivingDocRepositoryDsl
import info.novatec.testit.livingdoc.dsl.LivingDocResourceDsl
import info.novatec.testit.livingdoc.tasks.FreezeTask
import info.novatec.testit.livingdoc.tasks.RunLivingDocSpecsTask
import org.gradle.api.DefaultTask
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.bundling.Jar

class LivingDocPlugin implements Plugin<Project> {

  private Project project

  NamedDomainObjectContainer<LivingDocDsl> livingDocExtContainer

  Logger logger = Logging.getLogger(LivingDocPlugin.class)

  @Override
  public void apply(Project project) {
    this.project = project
    this.project.convention.plugins.livingDoc = new LivingDocPluginConvention()
    this.livingDocExtContainer = this.project.container(LivingDocDsl)
    this.livingDocExtContainer.all {
      it.repositories = this.project.container(LivingDocRepositoryDsl)
      it.resources = this.project.container(LivingDocResourceDsl)
    }
    this.project.extensions.add( this.project.LIVINGDOC_SOURCESET_NAME, livingDocExtContainer)
    this.project.apply(plugin: JavaPlugin)
    
    // create the default LivingDoc sourceSet and configurations livingdocCompile and livingdocRuntime
    SourceSet ldDefautSS = this.createExtensionSourceSet("") 
    // Let both configurations extend the compile/runtime test configurations
    this.project.configurations.getByName(ldDefautSS.getCompileConfigurationName()).extendsFrom(this.project.configurations.testCompile)
    this.project.configurations.getByName(ldDefautSS.getRuntimeConfigurationName()).extendsFrom(this.project.configurations.testRuntime)

    this.livingDocExtContainer.whenObjectAdded { LivingDocDsl livingdocExtension ->
      SourceSet extensionSourceSet = this.createExtensionSourceSet(livingdocExtension.name)
      // Let both extensionSourceSet compile/runtime configurations extend the default two configurations
      this.project.configurations.getByName(extensionSourceSet.getCompileConfigurationName()).extendsFrom(this.project.configurations."${ldDefautSS.getCompileConfigurationName()}")
      this.project.configurations.getByName(extensionSourceSet.getRuntimeConfigurationName()).extendsFrom(this.project.configurations."${ldDefautSS.getRuntimeConfigurationName()}")
      Jar compileFixturesTask = this.createCompileFixturesTask(livingdocExtension, extensionSourceSet)
      this.project.afterEvaluate {
        this.livingDocExtContainerCheckPrerequisite(livingdocExtension)
        this.configureSourceSet(livingdocExtension, extensionSourceSet)
        if (livingdocExtension.repositories) {
          this.manageFreezeFromRepositories(livingdocExtension)
          RunLivingDocSpecsTask runSpecsTask = this.createRunTasks(compileFixturesTask, livingdocExtension, extensionSourceSet)
          runSpecsTask.dependsOn compileFixturesTask
        }
      }
    }
  }

  /**
   * This method is executed as soon as a LivingDocExtention configuration is created
   */
  private SourceSet createExtensionSourceSet(String extensionName) {
    SourceSet extensionSourceSet = this.project.sourceSets.create("${this.project.LIVINGDOC_SOURCESET_NAME}${extensionName.capitalize()}")
    this.project.configurations.getByName(extensionSourceSet.getCompileConfigurationName()){ transitive = false }
    this.project.configurations.getByName(extensionSourceSet.getRuntimeConfigurationName()){ transitive = false }
    logger.info("Configuration {} created!!!", extensionSourceSet.getCompileConfigurationName())
    logger.info("Configuration {} created!!!", extensionSourceSet.getRuntimeConfigurationName())

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
  private configureSourceSet(LivingDocDsl livingdocExtension, SourceSet extensionSourceSet) {
    this.project.configure(extensionSourceSet) {
      logger.info("Configure sourceSet {}", extensionSourceSet.name)
      logger.info("{} fixtureSourceDirectory is {}", extensionSourceSet.name, livingdocExtension.fixtureSourceDirectory?.path)
      logger.info("{} resources directory is {}", extensionSourceSet.name,livingdocExtension.resources?.collect { it.directory?.path }?.iterator()?.join(', '))
      java.srcDirs this.project.file(livingdocExtension.fixtureSourceDirectory?.path)
      if (livingdocExtension.resources) {
        livingdocExtension.resources.each { resource ->
          resources.srcDirs this.project.file(resource.directory?.path)
        }
      }
    }
  }
  /**
   * This method creates the jar file from the livingdocExtention fixtureSourceDirectory path
   * 
   * @param livingdocExtension
   * @return
   */
  private Jar createCompileFixturesTask(LivingDocDsl livingdocExtension, SourceSet extensionSourceSet) {
    Jar compileFixturesTask = this.project.tasks.create("compile${livingdocExtension.name.capitalize()}Jar", Jar)
    this.project.configure(compileFixturesTask){
      group this.project.LIVINGDOC_TASKS_GROUP
      description "Compile the ${livingdocExtension.name} classes of the ${this.project} to a jar file"
      classifier = livingdocExtension.name
      version = this.project.version
      from extensionSourceSet.output
      destinationDir this.project.file("${project.buildDir}/${this.project.LIVINGDOC_SOURCESET_NAME}/${livingdocExtension.name}")
    }
    return compileFixturesTask
  }

  private void manageFreezeFromRepositories(LivingDocDsl livingdocExtension) {
    def freezeTasks = []
    livingdocExtension.repositories.each { LivingDocRepositoryDsl repository ->
      if (!repository.url || !repository.uid || !repository.implementation) {
        throw new Exception("Some of the required repository attributes (url, uid, implementation) from ${repository.name} are empty!")
      }
      freezeTasks += createFreezeTask(livingdocExtension, repository)
    }
    if (this.livingDocExtContainer.size() > 1) {
      DefaultTask freezeAllRepositoriesSpecsTask = this.project.tasks.maybeCreate("freezeAllSpecs", DefaultTask)
      this.project.configure(freezeAllRepositoriesSpecsTask) {
        group this.project.LIVINGDOC_TASKS_GROUP
        description "Freezes the livingDoc specifications of all repositories"
        dependsOn << freezeTasks
      }
    }
  }
  
  private FreezeTask createFreezeTask(LivingDocDsl livingdocExtension, LivingDocRepositoryDsl repository) {
    FreezeTask freezeTask = this.project.tasks.create("freeze${repository.name.capitalize()}Specs", FreezeTask)
    this.project.configure(freezeTask) {
      group this.project.LIVINGDOC_TASKS_GROUP
      description "Freezes the LivingDoc specifications of ${repository.name} repository"
      repositoryUrl repository.url
      repositoryUid repository.uid
      repositoryImplementation repository.implementation
      specsDirectory livingdocExtension.specsDirectory.path
      specificationFilter repository.filter
    }
    return freezeTask
  }

  /**
   * Creates a run task per LivingDocDsl configuration
   */
  private RunLivingDocSpecsTask createRunTasks(Jar compileFixturesTask, LivingDocDsl livingdocExtension, SourceSet extensionSourceSet) {
    RunLivingDocSpecsTask livingDocRunTask = project.tasks.create("run${this.project.LIVINGDOC_SOURCESET_NAME.capitalize()}${livingdocExtension.name.capitalize()}", RunLivingDocSpecsTask)
    def additionalClasspath = livingdocExtension.additionalRunClasspath ?: ""
    def additionalRunArgs = livingdocExtension.additionalRunArgs ?: []
    this.project.configure(livingDocRunTask) {
      group this.project.LIVINGDOC_TASKS_GROUP
      description "Run ${livingdocExtension.name} specifications from directory ${livingdocExtension.specsDirectory.path} on the ${this.project}"
      workingDir this.project.projectDir
      classPath additionalClasspath + File.pathSeparator + compileFixturesTask.archivePath.path + File.pathSeparator + extensionSourceSet.runtimeClasspath.asPath
      procArgs += [
        *additionalRunArgs,
        livingdocExtension.livingDocRunner,
        '-f',
        livingdocExtension.systemUnderDevelopment + ';' + livingdocExtension.sud,
        ((livingdocExtension.debug) ? '--debug' : ''),
        ((livingdocExtension.reportsType) ? '--' + livingdocExtension.reportsType : ''),
        '-s',
        livingdocExtension.specsDirectory?.path,
        '-o',
        ((livingdocExtension.reportsDirectory) ?  livingdocExtension.reportsDirectory.path : project.buildDir.path + '/livingdoc/reports')
      ]
      showOutput true
    }
    return livingDocRunTask
  }

  private livingDocExtContainerCheckPrerequisite(LivingDocDsl extension) {
    if (!extension.fixtureSourceDirectory || !extension.specsDirectory || !extension.systemUnderDevelopment) {
      throw new Exception("Some of the required attributes (fixtureSourceDirectory, specsDirectory, systemUnderDevelopment) from ${extension.name} are empty!")
    }
  }
}
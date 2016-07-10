package info.novatec.testit.livingdoc.plugins

import info.novatec.testit.livingdoc.conventions.LivingDocPluginConvention
import info.novatec.testit.livingdoc.dsl.FixtureDsl
import info.novatec.testit.livingdoc.dsl.RepositoryFixtureFilterDsl
import info.novatec.testit.livingdoc.dsl.LivingDocContainerDsl
import info.novatec.testit.livingdoc.dsl.RepositoryDsl
import info.novatec.testit.livingdoc.dsl.FixtureResourcesDsl
import info.novatec.testit.livingdoc.tasks.FreezeTask
import info.novatec.testit.livingdoc.tasks.RunLivingDocSpecsTask
import org.gradle.api.DefaultTask
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.bundling.Jar
import org.gradle.internal.reflect.Instantiator

class LivingDocPlugin implements Plugin<Project> {

  private Project project

  private livingdocDefaultSourceSet

  LivingDocContainerDsl livingDocExtension

  NamedDomainObjectContainer<FixtureDsl> fixturesContainer

  NamedDomainObjectContainer<RepositoryDsl> repositoriesContainer

  Logger logger = Logging.getLogger(LivingDocPlugin.class)

  @Override
  public void apply(Project project) {
    this.project = project
    this.project.apply(plugin: JavaPlugin)
    this.project.convention.plugins.livingDoc = new LivingDocPluginConvention()
    this.livingdocDefaultSourceSet = this.createDefaultSourceSet()

    this.livingDocExtension = this.project.extensions.create(this.project.LIVINGDOC_SOURCESET_NAME, LivingDocContainerDsl, this.project)

    this.fixturesContainer = this.livingDocExtension.extensions."${this.project.LIVINGDOC_FIXTURES_CONTAINER_NAME}" = this.project.container(FixtureDsl) { String name ->
      FixtureDsl fixture = this.project.gradle.services.get(Instantiator).newInstance(FixtureDsl, name, this.project)
      assert fixture instanceof ExtensionAware

      fixture.resources = this.project.container(FixtureResourcesDsl)
      this.createExtensionSourceSet(name, this.livingdocDefaultSourceSet)
      this.createCompileFixturesTask(fixture)

      return fixture
    }

    this.repositoriesContainer = this.livingDocExtension.extensions."${this.project.LIVINGDOC_REPOSITORIES_CONTAINER_NAME}" = this.project.container(RepositoryDsl) { String name ->
      RepositoryDsl repository = this.project.gradle.services.get(Instantiator).newInstance(RepositoryDsl, name, this.project)
      assert repository instanceof ExtensionAware

      repository.sortfilter = this.project.container(RepositoryFixtureFilterDsl)

      return repository
    }

    this.project.afterEvaluate {
      this.fixturesContainer.each { FixtureDsl fixture ->
        this.checkFixturePrerequisite(fixture)
        this.configureSourceSet(fixture)
        RunLivingDocSpecsTask runSpecsTask = this.createRunTasks(this.project.tasks."compile${fixture.name.capitalize()}Jar", fixture)
        runSpecsTask.dependsOn this.project.tasks."compile${fixture.name.capitalize()}Jar"
      }
      this.repositoriesContainer.each { RepositoryDsl repository ->
        this.createFreezeTask(repository)
      }
    }
  }

  /**
   * Creates the default LivingDoc sourceSet as well as let bot sourceSet configurations extend the testCompile and testRuntime configurations
   *
   * @return default livingdoc sourceSet
   */
  private SourceSet createDefaultSourceSet() {
    // create the default LivingDoc sourceSet and the both configurations livingdocCompile and livingdocRuntime
    SourceSet livindDocSourceSet = this.createExtensionSourceSet("", null)
    // Let both configurations extend the compile/runtime test configurations
    this.project.configurations.getByName(livindDocSourceSet.getCompileConfigurationName()).extendsFrom(this.project.configurations.testCompile)
    this.project.configurations.getByName(livindDocSourceSet.getRuntimeConfigurationName()).extendsFrom(this.project.configurations.testRuntime)
    return livindDocSourceSet
  }

  /**
   * This method is executed as soon as a FixtureDsl configuration is created
   */
  private SourceSet createExtensionSourceSet(String extensionName, SourceSet defaultSourceSet) {
    SourceSet sourceSet = this.project.sourceSets.create("${this.project.LIVINGDOC_SOURCESET_NAME}${extensionName.capitalize()}")
    this.project.configurations.getByName(sourceSet.getCompileConfigurationName()) { transitive = false }
    this.project.configurations.getByName(sourceSet.getRuntimeConfigurationName()) { transitive = false }
    logger.info("Configuration {} created!!!", sourceSet.getCompileConfigurationName())
    logger.info("Configuration {} created!!!", sourceSet.getRuntimeConfigurationName())

    if (defaultSourceSet != null) {
      // Let both fixtureSourceSet compile/runtime configurations extend the default two configurations
      this.project.configurations.getByName(sourceSet.getCompileConfigurationName()).extendsFrom(this.project.configurations."${defaultSourceSet.getCompileConfigurationName()}")
      this.project.configurations.getByName(sourceSet.getRuntimeConfigurationName()).extendsFrom(this.project.configurations."${defaultSourceSet.getRuntimeConfigurationName()}")
    }

    this.project.plugins.withType(JavaPlugin) {
      this.project.configure(sourceSet) {
        compileClasspath += this.project.sourceSets.getByName('main').output
        runtimeClasspath += compileClasspath
      }

      this.project.plugins.withType(org.gradle.plugins.ide.eclipse.EclipsePlugin) {
        this.project.eclipse {
          classpath {
            plusConfigurations.add(this.project.configurations.getByName(sourceSet.getCompileConfigurationName()))
            plusConfigurations.add(this.project.configurations.getByName(sourceSet.getRuntimeConfigurationName()))
          }
        }
      }
    }
    return sourceSet
  }

  /**
   * This method is executed after the Gradle build file of the project is fully initialized
   */
  private configureSourceSet(FixtureDsl fixture) {
    SourceSet fixtureSourceSet = this.getFixtureSourceSet(fixture)
    this.project.configure(fixtureSourceSet) {
      logger.info("Configure sourceSet {}", fixtureSourceSet.name)
      logger.info("{} fixtureSourceDirectory is {}", fixtureSourceSet.name, fixture.fixtureSourceDirectory?.path)
      logger.info("{} resources directory is {}", fixtureSourceSet.name, fixture.resources?.collect {
        it.directory?.path
      }?.iterator()?.join(', '))
      java.srcDirs this.project.file(fixture.fixtureSourceDirectory?.path)
      if (fixture.resources) {
        fixture.resources.each { resource ->
          resources.srcDirs this.project.file(resource.directory?.path)
        }
      }
    }
  }

  /**
   * This method creates the jar file from the compiled fixture classes
   *
   * @param fixture
   * @return the jar task
   */
  private Jar createCompileFixturesTask(FixtureDsl fixture) {
    SourceSet fixtureSourceSet = this.getFixtureSourceSet(fixture)
    Jar compileFixturesTask = this.project.tasks.create("compile${fixture.name.capitalize()}Jar", Jar)
    this.project.configure(compileFixturesTask) {
      group this.project.LIVINGDOC_TASKS_GROUP
      description "Compile the ${fixture.name} classes of the ${this.project} to a jar file"
      classifier = fixture.name
      version = this.project.version
      from fixtureSourceSet.output
      destinationDir this.project.file("${project.buildDir}${File.separator}${this.project.LIVINGDOC_SOURCESET_NAME}${File.separator}${fixture.name}")
    }
    return compileFixturesTask
  }

  /**
   * Cretes a freeze specification task pro configured repository
   * @param repository
   * @return
   */
  private createFreezeTask(RepositoryDsl repository) {
    FreezeTask task = this.project.tasks.create("freeze${repository.name.capitalize()}Specs", FreezeTask)
    this.project.configure(task) {
      group this.project.LIVINGDOC_TASKS_GROUP
      description "Freezes the LivingDoc specifications of ${repository.name} repository"
      repositoryUrl repository.url
      repositoryUid repository.uid
      repositoryImplementation repository.implementation
      specsDirectory repository.freezeDirectory
      specificationFilters = repository.sortfilter
    }
    logger.info("Task {} created for repository {}", task, repository.name)
  }

  /**
   * Creates a run task per fixture configuration
   */
  private RunLivingDocSpecsTask createRunTasks(Jar compileFixturesTask, FixtureDsl fixture) {
    SourceSet fixtureSourceSet = this.getFixtureSourceSet(fixture)
    RunLivingDocSpecsTask task = project.tasks.create("run${this.project.LIVINGDOC_SOURCESET_NAME.capitalize()}${fixture.name.capitalize()}", RunLivingDocSpecsTask)
    def additionalClasspath = fixture.additionalRunClasspath ?: ""
    def additionalRunArgs = fixture.additionalRunArgs ?: []
    this.project.configure(task) {
      group this.project.LIVINGDOC_TASKS_GROUP
      description "Run ${fixture.name} specifications from directory ${fixture.specsDirectory.path} on the ${this.project}"
      workingDir this.project.projectDir
      classPath additionalClasspath + File.pathSeparator + compileFixturesTask.archivePath.path + File.pathSeparator + fixtureSourceSet.runtimeClasspath.asPath
      procArgs += [
              *additionalRunArgs,
              fixture.livingDocRunner,
              '-f',
              fixture.systemUnderDevelopment + ';' + fixture.sud,
              ((fixture.debug) ? '--debug' : ''),
              ((fixture.reportsType) ? '--' + fixture.reportsType : ''),
              '-s',
              fixture.specsDirectory?.path,
              '-o',
              fixture.reportsDirectory
      ]
      showOutput true
    }
    logger.info("Task {} created for sourceSet {}", task, fixtureSourceSet)
    return task
  }

  private checkFixturePrerequisite(FixtureDsl fixture) {
    if (!fixture.fixtureSourceDirectory || !fixture.specsDirectory || !fixture.systemUnderDevelopment) {
      throw new Exception("Some of the required attributes (fixtureSourceDirectory, specsDirectory, systemUnderDevelopment) from ${fixture.name} are empty!")
    }
    def freezePaths = []
    if (!this.repositoriesContainer.isEmpty()) {
      this.repositoriesContainer.each { RepositoryDsl repository ->
        if (repository.sortfilter.isEmpty()) {
          freezePaths << repository.freezeDirectory.path
        } else {
          freezePaths += repository.sortfilter.collect { "${repository.freezeDirectory.path}${File.separator}${it.path}".toString() }
        }
      }
    }
    if (this.repositoriesContainer.isEmpty() || freezePaths.findAll { it.equals(fixture.specsDirectory.path) }.isEmpty()) {
      logger.warn("WARRNING: The fixture configuration {} specsDirectory path \"{}\" cannot be found in any freeze task configuration", fixture.name, fixture.specsDirectory)
    }
  }

  private SourceSet getFixtureSourceSet(FixtureDsl fixture) {
    return this.project.sourceSets.getByName("${this.project.LIVINGDOC_SOURCESET_NAME}${fixture.name.capitalize()}")
  }
}
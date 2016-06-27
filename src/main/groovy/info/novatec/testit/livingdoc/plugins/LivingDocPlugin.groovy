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

import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging

class LivingDocPlugin implements Plugin<Project> {
  
  private Project project
  
  private Jar compileFixturesTask
  
  private FreezeTask freezeTask
  
  private RunLivingDocSpecsTask runLivingDocSpecsTask
  
  private LivingDocExtension livingDocExt
  
  Logger logger = Logging.getLogger(LivingDocPlugin.class)
  
	@Override
	public void apply(Project project) {
    this.project = project
    this.project.convention.plugins.livingDoc = new LivingDocPluginConvention()
    this.livingDocExt = this.project.extensions.create( LivingDocExtension.NAME, LivingDocExtension, this.project )
    this.project.configurations.create("${this.project.LIVINGDOC_SOURCESET_NAME}Compile")
    this.project.configurations.create("${this.project.LIVINGDOC_SOURCESET_NAME}Runtime")
   
    /**
     * check whether the livingdoc extension is configured
     */
    this.project.afterEvaluate {
      if (true) {
        this.project.apply(plugin: org.gradle.api.plugins.JavaBasePlugin) // TODO check if needed
        this.createSourceSet()
        this.createCompileFixturesTask()
        this.createFreezeTask()
        this.createRunLivindDocTask()
      } else {
        // TODO Throw exception
        println "Bad Exception"
      }
    }
	}
  
  private createSourceSet() {
    SourceSet ldSourceSet = this.project.sourceSets.create(this.project.LIVINGDOC_SOURCESET_NAME)
    this.project.configure(ldSourceSet) {
      java.srcDirs this.project.file(this.livingDocExt.sourceDirectory.path) // may be only the path to the fixtures dir e.g. src/fixture/
      // TODO do we need something like this???
      //resources.srcDirs this.project.file("src/${dashSeparated}/resources")
    }
    this.project.plugins.withType(JavaPlugin) {
      this.project.configure(ldSourceSet) {
        compileClasspath += this.project.sourceSets.getByName('main').output
        runtimeClasspath += compileClasspath
      }
      
      this.project.plugins.withType(org.gradle.plugins.ide.eclipse.EclipsePlugin) {
        this.project.eclipse {
          classpath {
            plusConfigurations.add(this.project.configurations.getByName(sourceSetType.getCompileConfigurationName()))
            this.project.configurations.getByName(sourceSetType.getCompileConfigurationName()){ transitive = false }
            plusConfigurations.add(this.project.configurations.getByName(sourceSetType.getRuntimeConfigurationName()))
            this.project.configurations.getByName(sourceSetType.getRuntimeConfigurationName()){ transitive = false }
          }
        }
      }
    }
  }

  private void createCompileFixturesTask() {
    logger.info("{} has source sets {}", this.project.name, this.project.sourceSets*.name)
    this.compileFixturesTask = this.project.tasks.create("compile${this.project.LIVINGDOC_SOURCESET_NAME.capitalize()}Jar", Jar)
    this.project.configure(this.compileFixturesTask){
      group this.project.LIVINGDOC_TASKS_GROUP
      description "Compile the ${this.project.LIVINGDOC_SOURCESET_NAME} classes of the ${this.project.name} project to a jar file"
      classifier = this.project.LIVINGDOC_SOURCESET_NAME
      version = this.project.LIVINGDOC_TARGET_VERSION
      from this.project.sourceSets."${this.project.LIVINGDOC_SOURCESET_NAME}".output
      destinationDir project.file("${project.buildDir}/${this.project.LIVINGDOC_SOURCESET_NAME}")
    }
  }
  
  private void createFreezeTask() {
    this.freezeTask = this.project.tasks.create("freeze", FreezeTask)
    this.project.configure(this.freezeTask) {
      group this.project.LIVINGDOC_TASKS_GROUP
      description "Freezes the LivingDoc specifications"
      repositoryUrl this.livingDocExt.repositoryURL
      repositoryUid this.livingDocExt.repositoryUID
      specsDirectory this.livingDocExt.specsDirectory.path
      repositoryImplementation this.livingDocExt.respositoryImplementation
    }
  }
  
  private void createRunLivindDocTask() {
    this.runLivingDocSpecsTask = this.project.tasks.create("livingDoc${this.project.name.capitalize()}Run", RunLivingDocSpecsTask)
    this.runLivingDocSpecsTask.dependsOn this.compileFixturesTask, this.freezeTask
    this.project.configure(this.runLivingDocSpecsTask){
      group this.project.LIVINGDOC_TASKS_GROUP
      description "Run LivingDoc specifications from directory ${this.livingDocExt.specsDirectory.path} on the ${this.project}"
      workingDir this.project.buildDir // TODO Which is the working directory???
      classPath this.compileFixturesTask.archivePath.path + File.pathSeparator + this.project.sourceSets."${this.project.LIVINGDOC_SOURCESET_NAME}".runtimeClasspath.asPath
      procArgs += [
        'info.novatec.testit.livingdoc.runner.Main',
        '-f',
        "${this.livingDocExt.sudImplementation};${this.livingDocExt.sud}",
        '--debug', // TODO should be changed
        "--${this.livingDocExt.reportsType}",
        '-s',
        this.livingDocExt.specsDirectory.path,
        '-o',
        this.livingDocExt.reportsDirectory.path
        ]
      showOutput true
    }
  }
} 
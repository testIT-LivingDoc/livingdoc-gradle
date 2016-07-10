package info.novatec.testit.livingdoc.dsl

import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project
import org.gradle.api.file.FileCollection

/**
 * Created by nni on 08.07.16.
 */
class FixtureDsl {

  public String name

  public Project project

  public File fixtureSourceDirectory

  public File specsDirectory

  public File reportsDirectory

  public File runLivingdocDirectory

  public String systemUnderDevelopment = 'info.novatec.testit.livingdoc.systemunderdevelopment.DefaultSystemUnderDevelopment'

  public String reportsType

  public String sud

  public String livingDocRunner = 'info.novatec.testit.livingdoc.runner.Main'

  public List<String> additionalRunArgs

  public String additionalRunClasspath

  public Boolean debug = false

  public NamedDomainObjectContainer<FixtureResourcesDsl> resources

  public FixtureDsl(String name, Project project) {
    this.name = name
    this.project = project
    this.specsDirectory = new File("${project.buildDir.path}${File.separator}${project.LIVINGDOC_SOURCESET_NAME}${File.separator}specs")
    this.reportsDirectory = new File("${project.buildDir.path}${File.separator}${project.LIVINGDOC_SOURCESET_NAME}${File.separator}reports")
    this.runLivingdocDirectory = new File(project.projectDir.path)
  }

  public void resources(final Closure configureClosure) {
    resources.configure(configureClosure)
  }
}

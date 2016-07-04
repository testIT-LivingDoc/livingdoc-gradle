package info.novatec.testit.livingdoc.dsl

import java.io.File

import org.gradle.api.NamedDomainObjectContainer

class LivingDocDsl {
  
  public String name
  
  public File fixtureSourceDirectory

  public File specsDirectory

  public File reportsDirectory
  
  public String systemUnderDevelopment = 'info.novatec.testit.livingdoc.systemunderdevelopment.DefaultSystemUnderDevelopment'

  public String reportsType

  public String sud
  
  public String livingDocRunner = 'info.novatec.testit.livingdoc.runner.Main'
  
  public List<String> additionalRunArgs
  
  public String additionalRunClasspath

  public NamedDomainObjectContainer<LivingDocResourceDsl> resources
  
  public NamedDomainObjectContainer<LivingDocResourceDsl> repositories
  
  public Boolean debug = false
  
  public LivingDocDsl(String name) {
    this.name = name
  }
  
  public void resources(final Closure configureClosure) {
    resources.configure(configureClosure)
  }
  
  public void repositories(final Closure configureClosure) {
    repositories.configure(configureClosure)
  }
}

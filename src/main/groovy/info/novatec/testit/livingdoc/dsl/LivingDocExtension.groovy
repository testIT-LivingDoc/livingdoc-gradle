package info.novatec.testit.livingdoc.dsl

import org.gradle.api.Project

class LivingDocExtension {
  
  final static String NAME = 'livingdoc'
  
  // TODO should be relative to the project dir???
  def File sourceDirectory
  
  def File specsDirectory
  
  public LivingDocExtension(Project project) {
    this.sourceDirectory = new File("${project.projectDir}/src/fixtures/java")
    this.specsDirectory = new File("${project.buildDir}/livingdoc-specifications")
  }
}

package info.novatec.testit.livingdoc.dsl

import org.gradle.api.Project

class LivingDocExtension {
  
  final static String NAME = 'livingdoc'
  
  def String sudImplementation = "info.novatec.testit.livingdoc.systemunderdevelopment.DefaultSystemUnderDevelopment"
  
  def String respositoryImplementation = ""
  
  def String repositoryURL = ""
  
  def String repositoryUID = ""
  
  def String reportsType = "html"
  
  def String sud = ""
  
  def Boolean debug = false
  
  // TODO should be relative to the project dir???
  def File sourceDirectory
  
  def File specsDirectory
  
  def File reportsDirectory
  
  public LivingDocExtension(Project project) {
    this.sourceDirectory = new File("${project.projectDir}/src/fixtures/java")
    this.specsDirectory = new File("${project.buildDir}/livingdoc/specifications")
    this.reportsDirectory = new File("${project.buildDir}/livingdoc/reports")
  }
}

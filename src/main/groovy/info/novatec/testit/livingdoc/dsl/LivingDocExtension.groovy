package info.novatec.testit.livingdoc.dsl

import java.io.File;

class LivingDocExtension {
  
  public Boolean debug = false
  
  public File fixtureSourceDirectory

  public File specsDirectory

  public File resources

  public File reportsDirectory
  
  public String name
  
  public String sudImplementation = "info.novatec.testit.livingdoc.systemunderdevelopment.DefaultSystemUnderDevelopment"

  public String respositoryImplementation = ""

  public String repositoryURL = ""

  public String repositoryUID = ""

  public String reportsType = ""

  public String sud = ""
  
  public String livingDocRunner = 'info.novatec.testit.livingdoc.runner.Main'
  
  public LivingDocExtension(String name) {
    this.name = name
  }
}

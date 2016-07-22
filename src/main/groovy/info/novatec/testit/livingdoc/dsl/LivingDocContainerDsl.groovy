package info.novatec.testit.livingdoc.dsl

import org.gradle.api.Project

class LivingDocContainerDsl {

  public Project project

  public LivingDocContainerDsl(Project project) {
    this.project = project
  }

}

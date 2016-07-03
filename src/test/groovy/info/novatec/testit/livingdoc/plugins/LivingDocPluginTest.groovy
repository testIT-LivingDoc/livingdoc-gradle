package info.novatec.testit.livingdoc.plugins

import spock.lang.*
import org.gradle.api.*
import org.gradle.api.plugins.*
import org.gradle.testfixtures.ProjectBuilder

class LivingDocPluginTest extends Specification {

    Project project = ProjectBuilder.builder().build()
    
    def setup() {
        project.apply plugin: LivingDocPlugin
    }

    def "check initial setup"() {
      expect:
      project.apply plugin: JavaPlugin
      project.sourceSets.size() == 2
      
      when:
      project.livingdoc {}
      
      then:
      project.sourceSets.size() == 2
      project.tasks.findAll { it.name.startsWith(project.LIVINGDOC_SOURCESET_NAME) || it.name.startsWith('freeze')}.size() == 0
  }
    
    
  def "insert two livingdoc configuration"() {
      expect:
      project.apply plugin: JavaPlugin
      project.sourceSets.size() == 2
      
      when:
      project.livingdoc {
        intTest {
          repositories {
            localRepo {
              respositoryImplementation = "info.novatec.testit.livingdoc.repository.LivingDocRepository"
              repositoryURL = "REPOSITORY_URL"
              repositoryUID = "REPOSITORY_UID"
            }
          }
        }
        uiTest {
          repositories {
            localRepo {
              respositoryImplementation = "info.novatec.testit.livingdoc.repository.LivingDocRepository"
              repositoryURL = "REPOSITORY_URL"
              repositoryUID = "REPOSITORY_UID"
            }
          }
        }
      }
      
      then:
      project.sourceSets.size() == 4
      project.tasks.findAll { it.name.startsWith(project.LIVINGDOC_SOURCESET_NAME) || it.name.startsWith('freeze')}.size() == 0
  }
}

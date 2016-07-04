package info.novatec.testit.livingdoc.plugins

import spock.lang.*
import org.gradle.api.*
import org.gradle.api.plugins.*
import org.gradle.testfixtures.ProjectBuilder

/**
 * The test class for the {@link LivingDocPlugin}
 * 
 * @author Nikolay Nachev (NovaTec Consulting GmbH)
 *
 */
class LivingDocPluginTest extends Specification {

    Project project = ProjectBuilder.builder().build()
    
    def setup() {
        project.apply plugin: LivingDocPlugin
    }

    def "check initial setup"() {
      expect:
      project.apply plugin: JavaPlugin
      project.sourceSets.size() == 3
      
      when:
      project.livingdoc {}
      
      then:
      project.sourceSets.size() == 3
      project.tasks.findAll { it.name.startsWith(project.LIVINGDOC_SOURCESET_NAME) || it.name.startsWith('freeze')}.size() == 1
  }
    
    
  def "insert two livingdoc configuration"() {
      expect:
      project.apply plugin: JavaPlugin
      project.sourceSets.size() == 3
      
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
      project.sourceSets.size() == 5
      project.tasks.findAll { it.name.startsWith(project.LIVINGDOC_SOURCESET_NAME) || it.name.startsWith('freeze')}.size() == 3
  }
}

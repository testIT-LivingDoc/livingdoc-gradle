package info.novatec.testit.livingdoc.plugins

import spock.lang.*
import org.gradle.api.*
import org.gradle.testfixtures.ProjectBuilder

/**
 * The test class for the {@link LivingDocPlugin}
 *
 * @author Nikolay Nachev (NovaTec Consulting GmbH)
 *
 */
class LivingDocPluginTest extends Specification {

    Project project = ProjectBuilder.builder().build()

    def "check initial setup"() {
        expect:
        project.tasks.findAll {
            it.name.startsWith(project.LIVINGDOC_SOURCESET_NAME) || it.name.startsWith('freeze')
        }.size() == 0

        when:
        project.apply plugin: LivingDocPlugin

        then:
        project.sourceSets.size() == 3
        project.tasks.findAll {
            it.name.startsWith(project.LIVINGDOC_SOURCESET_NAME) && !it.name.startsWith('freeze')
        }.size() == 1
    }

    def "add two fixture configurations and check that the sourceSets are created"() {
        when:
        project.apply plugin: LivingDocPlugin
        project.livingdoc {
            fixtures {
                intTest {
                }
                uiTest {
                }
            }
        }

        then:
        project.sourceSets.size() == 5
        project.tasks.findAll {
            it.name.startsWith(project.LIVINGDOC_SOURCESET_NAME) && !it.name.startsWith('freeze')
        }.size() == 3
        project.sourceSets.findAll { it.name.startsWith(project.LIVINGDOC_SOURCESET_NAME) }.size() == 3
    }
}

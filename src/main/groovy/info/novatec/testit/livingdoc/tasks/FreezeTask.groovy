package info.novatec.testit.livingdoc.tasks

import info.novatec.testit.livingdoc.conventions.FreezeTaskConvention
import info.novatec.testit.livingdoc.conventions.LivingDocPluginConvention
import info.novatec.testit.livingdoc.document.Document
import info.novatec.testit.livingdoc.dsl.RepositoryFixtureFilterDsl
import info.novatec.testit.livingdoc.report.FileReportGenerator
import info.novatec.testit.livingdoc.report.Report
import info.novatec.testit.livingdoc.repository.DocumentRepository
import info.novatec.testit.livingdoc.utils.Repository;

import org.gradle.api.DefaultTask
import org.gradle.api.NamedDomainObjectCollection
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.plugins.ExtensionContainer
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction;

class FreezeTask extends DefaultTask {

  @Input
  String repositoryUrl

  @Input
  String repositoryUid

  @Input
  File specsDirectory

  @Input
  String repositoryImplementation

  @Input
  NamedDomainObjectContainer<RepositoryFixtureFilterDsl> specificationFilters

  @TaskAction
  void freezeSpecifications() {
    assert repositoryUrl != null, 'Error: The repositoryUrl cannot be null'
    assert repositoryUid != null, 'Error: The repositoryUid cannot be null'
    assert specsDirectory != null, 'Error: The freezeTargetDir cannot be null'
    assert repositoryImplementation != null, 'Error: The repositoryImplementation cannot be null'


    this.project.convention.plugins.freezeSpecsFilter = new FreezeTaskConvention()

    specsDirectory.deleteDir()
    specsDirectory.mkdir()

    logger.info("Start freezing specifications from '${repositoryUrl}' to directory '${specsDirectory.path}' with repositoryUid ${repositoryUid}.")

    Repository repository = new Repository();
    repository.setType(repositoryImplementation);
    repository.setRoot(repositoryUrl);

    this.freeze(repository.getDocumentRepository())

    logger.info("Freezing from specifications completed.")

    File specSourceDir = new File(specsDirectory, repositoryUid)

    if (!specSourceDir.exists() || this.dirEmpty(specSourceDir)) {
      logger.info("No specifications found.")
    } else {
      sortSpecifications(specSourceDir)
    }

    logger.info("Freezing and sorting specifications completed.")
  }

  private void freeze(DocumentRepository repository) {
    FileReportGenerator generator = new FileReportGenerator(specsDirectory);
    generator.adjustReportFilesExtensions(true);
    Document doc;
    Report report;

    List<String> specifications = repository.listDocuments(repositoryUid).findAll { it.toString() ==~ /${this.project.FREEZE_SPECS_FILTER}/ }
    
    specifications.each() { String specification ->
      doc = repository.loadDocument(specification);
      report = generator.openReport(specification);
      report.generate(doc);
      generator.closeReport(report);
      logger.info("Specification ${specification} was successful freezed")
    }
  }

  private sortSpecifications(File specSourceDir) {
    if (this.specificationFilters.isEmpty()) {
      project.copy {
        from(specSourceDir)
        into(new File(specsDirectory.path))
      }
    } else {
      this.specificationFilters.each { RepositoryFixtureFilterDsl fixtureFilter ->
        project.copy {
          from(specSourceDir) {
            include "${fixtureFilter.filter}"
          }
          into(new File(specsDirectory, fixtureFilter.path))
        }
      }
    }
    specSourceDir.deleteDir()
  }

  private boolean dirEmpty(File dir) {
    return dir.exists() && dir.directory && (dir.list() as List).empty
  }
}

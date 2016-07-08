package info.novatec.testit.livingdoc.tasks

import info.novatec.testit.livingdoc.document.Document
import info.novatec.testit.livingdoc.report.FileReportGenerator
import info.novatec.testit.livingdoc.report.Report
import info.novatec.testit.livingdoc.repository.DocumentRepository
import info.novatec.testit.livingdoc.utils.Repository;

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.TaskAction;

class FreezeTask extends DefaultTask {

  @Input
  String repositoryUrl
  
  @Input
  String repositoryUid
  
  @Input
  String specsDirectory
  
  @Input
  String repositoryImplementation

  @Input
  String specificationFilter

  @TaskAction
  void freezeSpecifications() {
    assert repositoryUrl != null, 'Error: The repositoryUrl cannot be null'
    assert repositoryUid != null, 'Error: The repositoryUid cannot be null'
    assert specsDirectory != null, 'Error: The freezeTargetDir cannot be null'
    assert repositoryImplementation != null, 'Error: The repositoryImplementation cannot be null'
    assert (specificationFilter != null && !specificationFilter.isEmpty()), 'Error: Specification filter cannot be null or empty'
    
    File specsDir = new File(specsDirectory);
    specsDir.deleteDir()
    specsDir.mkdir()

    logger.info("Start freezing specifications from '${repositoryUrl}' to directory '${specsDirectory}' with repositoryUid ${repositoryUid}.")

    Repository repository = new Repository();
    repository.setType(repositoryImplementation);
    repository.setRoot(repositoryUrl);
    
    this.freeze(repository.getDocumentRepository())

    logger.info("Freezing from specifications completed.")
  }
  
  private void freeze(DocumentRepository repository) {
    FileReportGenerator generator = new FileReportGenerator(new File(specsDirectory));
    generator.adjustReportFilesExtensions(true);
    Document doc;
    Report report;

    List<String> specifications = repository.listDocuments(repositoryUid).findAll { it ==~ specificationFilter }

    specifications.each() { String specification ->
      doc = repository.loadDocument(specification);
      report = generator.openReport(specification);
      report.generate(doc);
      generator.closeReport(report);
      logger.info("Specification ${specification} was successful freezed")
    }
  }
}

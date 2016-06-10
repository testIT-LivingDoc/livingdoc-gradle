package info.novatec.testit.livingdoc.tasks

import info.novatec.testit.livingdoc.conventions.FreezeTaskConvention
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
  
  @TaskAction
  void freezeSpecifications() {
    this.project.convention.plugins.freezeSpecification = new FreezeTaskConvention()
    logger.lifecycle("repositoryBaseUrl ${repositoryUrl}")
    logger.lifecycle("repositoryUid ${repositoryUid}")
    logger.lifecycle("freezeTargetDir ${specsDirectory}")

    assert repositoryUrl != null, 'repositoryUrl cannot be null'
    assert repositoryUid != null, 'repositoryUid cannot be null'
    assert specsDirectory != null, 'freezeTargetDir cannot be null'

    File specsDir = new File(specsDirectory);
    specsDir.deleteDir()
    specsDir.mkdir()

    logger.info("Start freezing specifications from '${repositoryUrl}' to directory '${specsDirectory}'.")

    Repository repository = new Repository();
    repository.setType(project.REPOSITORY_IMPLEMENTATION_CLASS.toString());
    repository.setRoot(repositoryUrl);
    
    freeze(repository.getDocumentRepository())
    
    File specSourceDir = new File(specsDirectory, repositoryUid)

    logger.info("Freezing and sorting specifications completed.")
  }
  
  private freeze(DocumentRepository repository) {
    FileReportGenerator generator = new FileReportGenerator(new File(specsDirectory));
    generator.adjustReportFilesExtensions(true);
    Document doc;
    Report report;

    List<String> specifications = repository.listDocuments(repositoryUid)

    specifications.each() { String specification ->
      doc = repository.loadDocument(specification);
      report = generator.openReport(specification);
      report.generate(doc);
      generator.closeReport(report);
      logger.lifecycle("Downloading: ${specification}")
    }
  }
}

package info.novatec.testit.livingdoc.tasks

import java.io.File;
import java.util.List;

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.TaskAction

class RunLivingDocSpecsTask extends DefaultTask {

  @InputDirectory
  File workingDir

  @Input
  String classPath

  @Input
  List<String> procArgs = []

  @Input
  Boolean showOutput = false

  @TaskAction
  void runLivingDoc() {

    List<String> processCmd = ["${System.properties.'java.home'}${File.separator}bin${File.separator}java".toString(), '-cp', classPath]
    processCmd += procArgs.findAll { !it.isEmpty() }.collect { it.toString() }
    logger.info("Execute the process with: {}", processCmd.iterator().join(' '))

    ProcessBuilder processBuilder = new ProcessBuilder(processCmd)
    processBuilder.redirectErrorStream(true)
    processBuilder.directory(workingDir)
    logger.info("Set the working dir of the process execution to: {}", workingDir)

    Process procRunner = processBuilder.start()

    if (showOutput) {
      procRunner.inputStream.eachLine { println it }
    }

    procRunner.waitFor();

    if (procRunner.exitValue() != 0) {
      throw new GradleException("Execution failed for task " + this.name)
    }
  }

}

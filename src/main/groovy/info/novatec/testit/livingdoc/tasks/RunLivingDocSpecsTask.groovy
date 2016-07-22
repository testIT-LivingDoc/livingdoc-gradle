package info.novatec.testit.livingdoc.tasks

import info.novatec.testit.livingdoc.conventions.LivingDocPluginConvention
import info.novatec.testit.livingdoc.conventions.RunLivingDocSpecsConvention
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputDirectories
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.SourceSet

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

    /**
     * This FileCollection should always contain only one file
     */
    @InputFiles
    FileCollection specsDirectory

    @OutputDirectory
    File reportsDirectory

    String classPath

    List<String> procArgs = []

    Boolean showOutput = false

    SourceSet fixtureSourceSet

    @TaskAction
    void runLivingDoc() {
        this.project.convention.plugins.runLivingdocSpecs = new RunLivingDocSpecsConvention()

        classPath += File.pathSeparator + fixtureSourceSet.getRuntimeClasspath().asPath

        List<String> processCmd = ["${this.project.LIVINGDOC_JAVA}${File.separator}bin${File.separator}java".toString(), '-cp', classPath]
        processCmd += procArgs.findAll { !it.toString().isEmpty() }.collect { it.toString() }
        processCmd += ['-s', specsDirectory.asPath]
        processCmd += ['-o', reportsDirectory.path]
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

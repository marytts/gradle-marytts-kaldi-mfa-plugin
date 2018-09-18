package de.dfki.mary.voicebuilding.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.tasks.*
import org.gradle.internal.os.OperatingSystem

class RunForcedAlignment extends DefaultTask {

    @InputDirectory
    final DirectoryProperty srcDir = newInputDirectory()

    @OutputDirectory
    final DirectoryProperty modelDir = newOutputDirectory()

    @OutputDirectory
    final DirectoryProperty destDir = newOutputDirectory()

    @TaskAction
    void run() {
        project.exec {
            commandLine = OperatingSystem.current().isWindows() ?
                    ['cmd', '/c', 'bin\\mfa_train_and_align.exe'] :
                    ['lib/train_and_align']
            commandLine += [
                    '--output_model_path', modelDir.get().asFile,
                    '--temp_directory', temporaryDir,
                    '--num_jobs',
                    project.gradle.startParameter.maxWorkerCount,
                    '--verbose',
                    srcDir.get().asFile, srcDir.file('dict.txt').get().asFile, destDir.get().asFile
            ]
            workingDir "$project.buildDir/mfa"
        }
    }
}

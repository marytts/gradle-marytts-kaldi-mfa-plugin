package de.dfki.mary.voicebuilding.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.*
import org.gradle.internal.os.OperatingSystem

class RunForcedAlignment extends DefaultTask {

    @InputDirectory
    File srcDir

    @OutputDirectory
    File modelDir = project.file("$project.buildDir/kaldiModels")

    @OutputDirectory
    File destDir = project.file("$project.buildDir/TextGrid")

    @TaskAction
    void run() {
        project.exec {
            commandLine = OperatingSystem.current().isWindows() ?
                    ['cmd', '/c', 'bin\\mfa_train_and_align.exe'] :
                    ['lib/train_and_align']
            commandLine += [
                    '--output_model_path', modelDir,
                    '--temp_directory', temporaryDir,
                    '--num_jobs',
                    project.gradle.startParameter.parallelProjectExecutionEnabled ? project.gradle.startParameter.maxWorkerCount : 1,
                    '--verbose',
                    srcDir, "$srcDir/dict.txt", destDir
            ]
            workingDir "$project.buildDir/mfa/montreal-forced-aligner"
        }
    }
}

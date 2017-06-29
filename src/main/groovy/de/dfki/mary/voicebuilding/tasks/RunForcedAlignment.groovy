package de.dfki.mary.voicebuilding.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.*

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
            commandLine 'docker', 'run', '--rm',
                    '--volume', "$modelDir:/models",
                    '--volume', "$srcDir:/data",
                    '--volume', "$temporaryDir:/temp",
                    '--volume', "$destDir:/textGrids",
                    '--tty', 'psibre/kaldi-mfa',
                    '/mfa/dist/montreal-forced-aligner/bin/mfa_train_and_align',
                    '--output_model_path', '/models',
                    '--temp_directory', '/temp',
                    '--num_jobs', project.gradle.startParameter.parallelProjectExecutionEnabled ? project.gradle.startParameter.maxWorkerCount : 1,
                    '--verbose',
                    '/data', '/data/dict.txt', '/textGrids'
        }
    }
}

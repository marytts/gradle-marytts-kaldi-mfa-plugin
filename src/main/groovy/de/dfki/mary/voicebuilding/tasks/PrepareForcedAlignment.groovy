package de.dfki.mary.voicebuilding.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.file.*
import org.gradle.api.tasks.*

class PrepareForcedAlignment extends DefaultTask {

    @InputDirectory
    final DirectoryProperty wavDir = project.objects.directoryProperty()

    @InputDirectory
    final DirectoryProperty mfaLabDir = project.objects.directoryProperty()

    @InputFile
    final RegularFileProperty dictFile = project.objects.fileProperty()

    @OutputDirectory
    final DirectoryProperty destDir = project.objects.directoryProperty()

    @TaskAction
    void prepare() {
        project.copy {
            from wavDir, {
                include '*.wav'
            }
            from mfaLabDir, {
                include '*.lab'
            }
            from dictFile
            into destDir
        }
    }
}

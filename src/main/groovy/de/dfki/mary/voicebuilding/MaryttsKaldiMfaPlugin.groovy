package de.dfki.mary.voicebuilding

import de.dfki.mary.voicebuilding.tasks.PrepareForcedAlignment
import org.gradle.api.Plugin
import org.gradle.api.Project

class MaryttsKaldiMfaPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        project.task('prepareForcedAlignment', type: PrepareForcedAlignment)
    }
}

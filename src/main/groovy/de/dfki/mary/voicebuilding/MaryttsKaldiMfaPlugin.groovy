package de.dfki.mary.voicebuilding

import de.dfki.mary.voicebuilding.tasks.*
import org.gradle.api.Plugin
import org.gradle.api.Project

class MaryttsKaldiMfaPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        project.configurations {
            marytts
        }
        project.repositories {
            jcenter()
        }
        project.dependencies {
            marytts 'de.dfki.mary:marytts-voicebuilding:0.1'
            marytts 'de.dfki.mary:marytts-lang-en:5.2'
        }
        project.task('convertTextToMaryXml', type: ConvertTextToMaryXML)
        project.task('prepareForcedAlignment', type: PrepareForcedAlignment) {
            dependsOn project.convertTextToMaryXml
            maryXmlDir = project.convertTextToMaryXml.destDir
        }
        project.task('runForcedAlignment', type: RunForcedAlignment) {
            dependsOn project.prepareForcedAlignment
            srcDir = project.prepareForcedAlignment.forcedAlignmentDir
        }
        project.task('convertTextGridToXLab', type: ConvertTextGridToXLab) {
            dependsOn project.runForcedAlignment
            tgDir = project.runForcedAlignment.destDir
        }
    }
}

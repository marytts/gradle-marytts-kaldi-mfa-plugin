package de.dfki.mary.voicebuilding

import de.dfki.mary.voicebuilding.tasks.*
import de.undercouch.gradle.tasks.download.*
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.Copy

class MaryttsKaldiMfaPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        project.pluginManager.apply DownloadTaskPlugin
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
        project.task('downloadMFA', type: Download) {
            src 'https://github.com/MontrealCorpusTools/Montreal-Forced-Aligner/releases/download/v1.0.0/montreal-forced-aligner_macosx.zip'
            dest project.buildDir
            overwrite false
        }
        project.task('unpackMFA', type: Copy) {
            dependsOn project.downloadMFA
            from project.zipTree("$project.buildDir/montreal-forced-aligner_macosx.zip")
            into "$project.buildDir/mfa"
        }
        project.task('runForcedAlignment', type: RunForcedAlignment) {
            dependsOn project.prepareForcedAlignment, project.unpackMFA
            srcDir = project.prepareForcedAlignment.forcedAlignmentDir
        }
        project.task('convertTextGridToXLab', type: ConvertTextGridToXLab) {
            dependsOn project.runForcedAlignment
            tgDir = project.runForcedAlignment.destDir
        }
    }
}

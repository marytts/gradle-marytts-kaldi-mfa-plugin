package de.dfki.mary.voicebuilding

import de.dfki.mary.voicebuilding.tasks.*
import de.undercouch.gradle.tasks.download.*
import org.gradle.api.*
import org.gradle.api.tasks.Copy
import org.gradle.internal.os.OperatingSystem

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
            ext.dep = getMFADependencyFor(project)
            src dep.url
            dest "$project.buildDir/$dep.name"
            overwrite false
        }

        project.task('unpackMFA', type: Copy) {
            dependsOn project.downloadMFA
            from OperatingSystem.current().isLinux() ? project.tarTree("$project.buildDir/mfa.tar.gz") :
                    project.zipTree("$project.buildDir/mfa.zip")
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

    Map getMFADependencyFor(Project project) {
        def os = OperatingSystem.current()
        def group = 'ca.mcgill.linguistics'
        def name = 'montreal-forced-aligner'
        def version = '1.0.0'
        def classifier
        def ext = 'zip'
        switch (os) {
            case { it.isLinux() }:
                classifier = 'linux'
                ext = 'tar.gz'
                break
            case { it.isMacOsX() }:
                classifier = 'macosx'
                break
            case { it.isWindows() && System.getenv("ProgramFiles(x86)") }:
                classifier = 'win64'
                break
            default:
                project.logger.error "Cannot determine native Montreal Forcer Aligner dependency for $os.name"
                return
        }
        [
                url : "https://github.com/MontrealCorpusTools/Montreal-Forced-Aligner/releases/download/v$version/${name}_${classifier}.$ext",
                name: "mfa.$ext"
        ]
    }
}

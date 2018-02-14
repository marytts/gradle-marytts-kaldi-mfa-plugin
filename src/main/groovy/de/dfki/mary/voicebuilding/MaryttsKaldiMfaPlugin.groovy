package de.dfki.mary.voicebuilding

import de.dfki.mary.voicebuilding.tasks.*
import org.gradle.api.*
import org.gradle.api.tasks.Copy
import org.gradle.internal.os.OperatingSystem

class MaryttsKaldiMfaPlugin implements Plugin<Project> {

    final String mfaVersion = '1.0.0'

    @Override
    void apply(Project project) {

        project.configurations {
            marytts
            mfa
        }

        project.repositories {
            jcenter()
            ivy {
                url "https://cdn.rawgit.com/marytts/montreal-forced-aligner-release-assets/$mfaVersion"
            }
        }

        project.dependencies {
            marytts 'de.dfki.mary:marytts-voicebuilding:0.1'
            marytts 'de.dfki.mary:marytts-lang-en:5.2'
            mfa getMFADependencyFor(project)
        }

        project.task('convertTextToMaryXml', type: ConvertTextToMaryXML) {
            srcDir = project.layout.buildDirectory.dir('text')
            destDir = project.layout.buildDirectory.dir('maryxml')
        }

        project.task('prepareForcedAlignment', type: PrepareForcedAlignment) {
            maryXmlDir = project.convertTextToMaryXml.destDir
            forcedAlignmentDir = project.layout.buildDirectory.dir('forcedAlignment')
        }

        project.task('unpackMFA', type: Copy) {
            from project.configurations.mfa
            into "$project.buildDir/mfa"
            filesMatching '*.zip', { zipFileDetails ->
                project.copy {
                    from project.zipTree(zipFileDetails.file)
                    into destinationDir
                }
                zipFileDetails.exclude()
            }
            filesMatching '*.tar.gz', { tarFileDetails ->
                project.copy {
                    from project.tarTree(tarFileDetails.file)
                    into destinationDir
                }
                tarFileDetails.exclude()
            }
        }

        project.task('runForcedAlignment', type: RunForcedAlignment) {
            dependsOn project.unpackMFA
            srcDir = project.prepareForcedAlignment.forcedAlignmentDir
            modelDir = project.layout.buildDirectory.dir('kaldiModels')
            destDir = project.layout.buildDirectory.dir('TextGrid')
        }

        project.task('convertTextGridToXLab', type: ConvertTextGridToXLab) {
            srcDir = project.runForcedAlignment.destDir
            destDir = project.layout.buildDirectory.dir('lab')
        }
    }

    Map getMFADependencyFor(Project project) {
        def os = OperatingSystem.current()
        def group = 'com.github.montrealcorpustools'
        def name = 'montreal-forced-aligner'
        def version = mfaVersion
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
                group     : group,
                name      : name,
                version   : version,
                classifier: classifier,
                ext       : ext
        ]
    }
}

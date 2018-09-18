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
                url 'https://github.com/marytts/montreal-forced-aligner-release-assets/archive'
                layout 'pattern', {
                    artifact '[revision]-[classifier].[ext]'
                }
            }
        }

        project.dependencies {
            marytts 'de.dfki.mary:marytts-voicebuilding:0.1'
            marytts 'de.dfki.mary:marytts-lang-en:5.2'
            mfa getMFADependencyFor(project)
        }

        project.task('convertTextToMaryXml', type: ConvertTextToMaryXml) {
            group = 'MFA'
            description = 'Converts text files to MaryXML for pronunciation prediction (G2P)'
            srcDir = project.layout.buildDirectory.dir('text')
            destDir = project.layout.buildDirectory.dir('maryxml')
        }

        project.task('processMaryXml', type: ProcessMaryXml) {
            group = 'MFA'
            description = 'Extracts text input files from MaryXML and generates custom dictionary for MFA'
            srcDir = project.convertTextToMaryXml.destDir
            destDir = project.layout.buildDirectory.dir('mfaLab')
            dictFile = project.layout.buildDirectory.file('dict.txt')
        }

        project.task('prepareForcedAlignment', type: PrepareForcedAlignment) {
            group = 'MFA'
            description = 'Collects audio and text input files and custom dictionary for MFA'
            wavDir = project.layout.buildDirectory.dir('wav')
            mfaLabDir = project.processMaryXml.destDir
            dictFile = project.processMaryXml.dictFile
            destDir = project.layout.buildDirectory.dir('forcedAlignment')
        }

        project.task('unpackMFA', type: Copy) {
            group = 'MFA'
            description = 'Downloads and unpacks MFA'
            from project.configurations.mfa
            into "$project.buildDir/mfa"
            filesMatching '*.zip', { zipFileDetails ->
                project.copy {
                    from project.zipTree(zipFileDetails.file)
                    into destinationDir
                    eachFile {
                        it.path = it.path.replaceAll(~/montreal-forced-aligner-release-assets-$mfaVersion-(macosx|linux|win64)/, 'montreal-forced-aligner')
                    }
                }
                zipFileDetails.exclude()
            }
            filesMatching '*.tar.gz', { tarFileDetails ->
                project.copy {
                    from project.tarTree(tarFileDetails.file)
                    into destinationDir
                    eachFile {
                        it.path = it.path.replaceAll(~/montreal-forced-aligner-release-assets-$mfaVersion-(macosx|linux|win64)/, 'montreal-forced-aligner')
                    }
                }
                tarFileDetails.exclude()
            }
        }

        project.task('runForcedAlignment', type: RunForcedAlignment) {
            group = 'MFA'
            description = 'Runs MFA to generate Praat TextGrids'
            dependsOn project.unpackMFA
            srcDir = project.prepareForcedAlignment.destDir
            modelDir = project.layout.buildDirectory.dir('kaldiModels')
            destDir = project.layout.buildDirectory.dir('TextGrid')
        }

        project.task('convertTextGridToXLab', type: ConvertTextGridToXLab) {
            group = 'MFA'
            description = 'Converts Praat TextGrids to XWaves lab format (with label mapping)'
            srcDir = project.runForcedAlignment.destDir
            tiername = 'phones'
            labelMapping = [sil: '_', sp: '_']
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

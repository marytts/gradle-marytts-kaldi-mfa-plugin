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
            mavenCentral()

            exclusiveContent {
                forRepository {
                    maven {
                        url 'https://mlt.jfrog.io/artifactory/mlt-mvn-releases-local'
                    }
                }
                filter {
                    includeGroup 'de.dfki.lt.jtok'
                }
            }

            ivy {
                url 'https://github.com/marytts/montreal-forced-aligner-release-assets/archive'
                patternLayout {
                    artifact '[revision]-[classifier].[ext]'
                }
                metadataSources {
                    artifact()
                }
            }
        }

        project.dependencies {
            marytts group: 'de.dfki.mary', name: 'marytts-voicebuilding', version: '0.2.1'
            marytts 'de.dfki.mary:marytts-lang-en:5.2.1', {
                exclude group: 'com.twmacinta', module: 'fast-md5'
                exclude group: 'gov.nist.math', module: 'Jampack'
            }
            mfa getMFADependencyFor(project)
        }

        def convertTextToMaryXmlTask = project.tasks.register('convertTextToMaryXml', ConvertTextToMaryXml) {
            group = 'MFA'
            description = 'Converts text files to MaryXML for pronunciation prediction (G2P)'
            srcDir.set project.layout.buildDirectory.dir('text')
            locale.set Locale.US
            destDir.set project.layout.buildDirectory.dir('maryxml')
        }

        def processMaryXmlTask = project.tasks.register('processMaryXml', ProcessMaryXml) {
            group = 'MFA'
            description = 'Extracts text input files from MaryXML and generates custom dictionary for MFA'
            srcDir.set convertTextToMaryXmlTask.get().destDir
            destDir.set project.layout.buildDirectory.dir('mfaLab')
            dictFile.set project.layout.buildDirectory.file('dict.txt')
        }

        def prepareForcedAlignmentTask = project.tasks.register('prepareForcedAlignment', PrepareForcedAlignment) {
            group = 'MFA'
            description = 'Collects audio and text input files and custom dictionary for MFA'
            wavDir.set project.layout.buildDirectory.dir('wav')
            mfaLabDir.set processMaryXmlTask.get().destDir
            dictFile.set processMaryXmlTask.get().dictFile
            destDir.set project.layout.buildDirectory.dir('forcedAlignment')
        }

        project.tasks.register('unpackMFA', Copy) {
            group = 'MFA'
            description = 'Downloads and unpacks MFA'
            from project.configurations.mfa
            into "$project.buildDir/mfa"
            filesMatching '*.zip', { zipFileDetails ->
                project.copy {
                    from project.zipTree(zipFileDetails.file)
                    into destinationDir
                    eachFile {
                        it.path = it.path.replaceAll(~/montreal-forced-aligner-release-assets-$mfaVersion-(macosx|linux|win64)/, '')
                    }
                    includeEmptyDirs = false
                }
                zipFileDetails.exclude()
            }
            filesMatching '*.tar.gz', { tarFileDetails ->
                project.copy {
                    from project.tarTree(tarFileDetails.file)
                    into destinationDir
                    eachFile {
                        it.path = it.path.replaceAll(~/montreal-forced-aligner-release-assets-$mfaVersion-(macosx|linux|win64)/, '')
                    }
                    includeEmptyDirs = false
                }
                tarFileDetails.exclude()
            }
        }

        def runForcedAlignmentTask = project.tasks.register('runForcedAlignment', RunForcedAlignment) {
            group = 'MFA'
            description = 'Runs MFA to generate Praat TextGrids'
            dependsOn project.unpackMFA
            srcDir.set prepareForcedAlignmentTask.get().destDir
            modelDir.set project.layout.buildDirectory.dir('kaldiModels')
            destDir.set project.layout.buildDirectory.dir('TextGrid')
            speakerChars.set 0
            fast.set false
            numJobs.set project.gradle.startParameter.maxWorkerCount
            noDict.set false
            clean.set false
            debug.set false
            ignoreExceptions.set false
        }

        project.tasks.register('convertTextGridToXLab', ConvertTextGridToXLab) {
            group = 'MFA'
            description = 'Converts Praat TextGrids to XWaves lab format (with label mapping)'
            srcDir.set runForcedAlignmentTask.get().destDir
            tiername.set 'phones'
            labelMapping.set([sil: '_', sp: '_'])
            destDir.set project.layout.buildDirectory.dir('lab')
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

package de.dfki.mary.voicebuilding.tasks

import org.gradle.api.*
import org.gradle.api.tasks.*

class PrepareForcedAlignment extends DefaultTask {

    @InputDirectory
    File maryXmlDir

    @InputDirectory
    File wavDir = project.file("$project.buildDir/wav")

    @OutputDirectory
    File forcedAlignmentDir = project.file("$project.buildDir/forcedAlignment")

    @TaskAction
    void prepare() {
        def dict = [:]
        def broken = []
        project.fileTree(maryXmlDir).include('*.xml').each { xmlFile ->
            try {
                def tokens = []
                new XmlSlurper().parse(xmlFile).'**'.findAll { it.name() == 't' }.each { token ->
                    def phonemes = token.'**'.findAll { it.name() == 'syllable' }.collect { syllable ->
                        syllable.ph.collect { it.@p }
                    }.flatten().join(' ')
                    if (phonemes) {
                        dict[token.text()] = phonemes
                        tokens << token.text()
                    }
                }
                project.file("$forcedAlignmentDir/${xmlFile.name - '.xml' + '.lab'}").withWriter('UTF-8') { out ->
                    out.println tokens.join(' ')
                }
            } catch (all) {
                project.logger.error "Excluding $xmlFile.name"
                broken << xmlFile.name - '.xml'
            }
        }
        project.file("$forcedAlignmentDir/dict.txt").withWriter('UTF-8') { out ->
            dict.toSorted { it.key.toLowerCase() }.each { word, phonemes ->
                out.println "$word $phonemes"
            }
        }
        project.copy {
            from wavDir
            into forcedAlignmentDir
            include '*.wav'
            exclude broken.collect { it + '.wav' }
        }
    }
}

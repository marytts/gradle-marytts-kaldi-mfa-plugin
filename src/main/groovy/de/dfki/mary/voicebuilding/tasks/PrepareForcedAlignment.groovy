package de.dfki.mary.voicebuilding.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.tasks.*

class PrepareForcedAlignment extends DefaultTask {

    @InputDirectory
    final DirectoryProperty maryXmlDir = newInputDirectory()

    @InputDirectory
    final DirectoryProperty wavDir = newInputDirectory()

    @OutputDirectory
    final DirectoryProperty forcedAlignmentDir = newOutputDirectory()

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
                forcedAlignmentDir.file(xmlFile.name - '.xml' + '.lab').get().asFile.withWriter('UTF-8') { out ->
                    out.println tokens.join(' ')
                }
            } catch (all) {
                project.logger.error "Excluding $xmlFile.name"
                broken << xmlFile.name - '.xml'
            }
        }
        forcedAlignmentDir.file('dict.txt').get().asFile.withWriter('UTF-8') { out ->
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

package de.dfki.mary.voicebuilding.tasks

import groovy.xml.XmlSlurper
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.*

class ProcessMaryXml extends DefaultTask {

    @InputDirectory
    final DirectoryProperty srcDir = project.objects.directoryProperty()

    @OutputDirectory
    final DirectoryProperty destDir = project.objects.directoryProperty()

    @OutputFile
    final RegularFileProperty dictFile = project.objects.fileProperty()

    @TaskAction
    void convert() {
        def broken = []
        def dict = [:]
        project.fileTree(srcDir).include('*.xml').each { xmlFile ->
            try {
                def tokens = []
                new XmlSlurper().parse(xmlFile).depthFirst().findAll { it.name() == 't' }.each { token ->
                    def word = token.text().trim()
                    // strip trailing dots, convert to lower case
                    word = word.replaceAll(/\.+$/, '').toLowerCase()
                    if (word) {
                        tokens << word
                        def phonemes = token.'**'.findAll { it.name() == 'syllable' }.collect { syllable ->
                            syllable.ph.collect { it.@p }
                        }.flatten().join(' ')
                        if (phonemes) {
                            dict[word] = phonemes
                        }
                    }
                }
                destDir.file(xmlFile.name - '.xml' + '.lab').get().asFile.withWriter('UTF-8') { out ->
                    out.println tokens.join(' ')
                }
            } catch (all) {
                project.logger.error "Excluding $xmlFile.name"
                broken << xmlFile.name - '.xml'
            }
        }
        dictFile.get().asFile.withWriter('UTF-8') { out ->
            dict.toSorted { it.key.toLowerCase() }.each { word, phonemes ->
                out.println "$word $phonemes"
            }
        }
    }
}

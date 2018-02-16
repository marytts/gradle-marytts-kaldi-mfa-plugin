import org.gradle.api.DefaultTask
import org.gradle.api.file.*
import org.gradle.api.tasks.*

class GenerateDictionaryFromMaryXml extends DefaultTask {

    @InputDirectory
    final DirectoryProperty srcDir = newInputDirectory()

    @OutputFile
    final RegularFileProperty destFile = newOutputFile()

    @TaskAction
    void generate() {
        def dict = [:]
        project.fileTree(srcDir).include('*.xml').each { xmlFile ->
            try {
                new XmlSlurper().parse(xmlFile).depthFirst().findAll { it.name() == 't' }.each { token ->
                    def phonemes = token.'**'.findAll { it.name() == 'syllable' }.collect { syllable ->
                        syllable.ph.collect { it.@p }
                    }.flatten().join(' ')
                    if (phonemes) {
                        dict[token.text()] = phonemes
                    }
                }
            } catch (all) {
                project.logger.error "Excluding $xmlFile.name"
            }
        }
        destFile.get().asFile.withWriter('UTF-8') { out ->
            dict.toSorted { it.key.toLowerCase() }.each { word, phonemes ->
                out.println "$word $phonemes"
            }
        }
    }
}

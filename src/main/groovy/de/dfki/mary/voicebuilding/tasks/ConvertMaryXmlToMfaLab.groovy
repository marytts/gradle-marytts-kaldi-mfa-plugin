import org.gradle.api.DefaultTask
import org.gradle.api.file.*
import org.gradle.api.tasks.*

class ConvertMaryXmlToMfaLab extends DefaultTask {

    @InputDirectory
    final DirectoryProperty srcDir = newInputDirectory()

    @OutputDirectory
    final DirectoryProperty destDir = newOutputDirectory()

    @TaskAction
    void convert() {
        def broken = []
        project.fileTree(srcDir).include('*.xml').each { xmlFile ->
            try {
                def tokens = []
                new XmlSlurper().parse(xmlFile).depthFirst().findAll { it.name() == 't' }.each { token ->
                    tokens << token.text()
                }
                destDir.file(xmlFile.name - '.xml' + '.lab').get().asFile.withWriter('UTF-8') { out ->
                    out.println tokens.join(' ')
                }
            } catch (all) {
                project.logger.error "Excluding $xmlFile.name"
                broken << xmlFile.name - '.xml'
            }
        }
    }
}

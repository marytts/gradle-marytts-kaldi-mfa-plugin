package de.dfki.mary.voicebuilding.tasks

import groovy.json.JsonBuilder
import org.gradle.api.DefaultTask
import org.gradle.api.JavaVersion
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.*

class ConvertTextToMaryXml extends DefaultTask {

    @Input
    final Property<Locale> locale = project.objects.property(Locale)

    @InputDirectory
    final DirectoryProperty srcDir = project.objects.directoryProperty()

    @OutputDirectory
    final DirectoryProperty destDir = project.objects.directoryProperty()

    @TaskAction
    void convert() {
        def localeStr = locale.get().toLanguageTag()
        def requests = project.fileTree(srcDir).include('*.txt').collect { txtFile ->
            def xmlFile = destDir.file(txtFile.name - '.txt' + '.xml').get().asFile
            [locale          : localeStr,
             inputType       : 'TEXT',
             inputFile       : txtFile.path,
             outputType      : 'ALLOPHONES',
             outputTypeParams: null,
             outputFile      : xmlFile.path]
        }
        def jsonFile = project.file("$temporaryDir/requests.json")
        jsonFile.text = new JsonBuilder(requests).toPrettyString()
        project.javaexec {
            classpath project.configurations.marytts
            mainClass = 'marytts.BatchProcessor'
            args = [jsonFile]
            if (JavaVersion.current().java9Compatible) {
                jvmArgs = [
                        '--add-opens', 'java.base/java.io=ALL-UNNAMED',
                        '--add-opens', 'java.base/java.lang=ALL-UNNAMED',
                        '--add-opens', 'java.base/java.util=ALL-UNNAMED'
                ]
            }
        }
    }
}

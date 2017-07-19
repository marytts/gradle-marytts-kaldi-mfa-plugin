package de.dfki.mary.voicebuilding.tasks

import groovy.json.JsonBuilder
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.*

class ConvertTextToMaryXML extends DefaultTask {
    @InputDirectory
    File srcDir = project.file("text")

    @OutputDirectory
    File destDir = project.file("$project.buildDir/maryxml")

    @TaskAction
    void convert() {
        def requests = project.fileTree(srcDir).include('*.txt').collect { txtFile ->
            def xmlFile = project.file("$destDir/${txtFile.name - '.txt' + '.xml'}")
            [locale          : 'en_US',
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
            main = 'marytts.BatchProcessor'
            args = [jsonFile]
        }
    }
}

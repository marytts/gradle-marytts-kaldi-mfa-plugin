package de.dfki.mary.voicebuilding.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.*

import org.m2ci.msp.jtgt.io.*

class ConvertTextGridToXLab extends DefaultTask {

    @InputDirectory
    final DirectoryProperty srcDir = newInputDirectory()

    @Input
    final Property<Map> labelMapping = project.objects.property(Map)

    @Input
    final Property<String> tiername = project.objects.property(String)

    @OutputDirectory
    final DirectoryProperty destDir = newOutputDirectory()

    @TaskAction
    void convert() {
        def tgSer = new TextGridSerializer()
        def xLabSer = new XWaveLabelSerializer()
        project.fileTree(srcDir).include('**/*.TextGrid').collect { tgFile ->
            def tg = tgSer.fromString(tgFile.text)
            tg.tiers.find { it.name == tiername.get() }.annotations.each {
                it.text = labelMapping.get()[it.text] ?: it.text
                if (it.text == '') {
                    it.text = "_"
                }
            }

            def xlabStr = xLabSer.toString(tg, tiername.get())

            def xlabFile = destDir.file(tgFile.name - '.TextGrid' + '.lab').get().asFile
            xlabFile.text = xlabStr
        }
    }

}

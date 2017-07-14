package de.dfki.mary.voicebuilding.tasks

import org.gradle.api.*
import org.gradle.api.tasks.*

import org.m2ci.msp.jtgt.io.TextGridSerializer
import org.m2ci.msp.jtgt.io.XWaveLabelSerializer

class ConvertTextGridToXLab extends DefaultTask {

    @InputDirectory
    File tgDir

    @OutputDirectory
    File destDir = project.file("$project.buildDir/lab")

    @TaskAction
    void convert() {
        def tgSer = new TextGridSerializer()
        def xLabSer = new XWaveLabelSerializer()
        project.fileTree("$tgDir/data").include('*.TextGrid').collect { tgFile ->
            def tg = tgSer.fromString(tgFile.text)

            def xlabStr = xLabSer.toString(tg, 'phones')
            //replace Strings (silence) that cannot be processed by MaryTTS
            xlabStr = xlabStr.replaceAll( "text = \"sil\"", "text = \"_\"")
            xlabStr = xlabStr.replaceAll( "text = \"\"", "text = \"_\"")

            def xlabFile = project.file("$destDir/${tgFile.name - '.TextGrid' + '.lab'}")
            xlabFile.text = xlabStr
        }
    }
}

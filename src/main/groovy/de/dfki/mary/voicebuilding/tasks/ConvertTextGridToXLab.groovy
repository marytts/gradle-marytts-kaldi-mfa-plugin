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
            //replace Strings (silence) that cannot be processed by MaryTTS
//            tgString = tgString.replaceAll( "text = \"sil\"", "text = \"_\"")
//            tgString = tgString.replaceAll( "text = \"\"", "text = \"_\"")
            def xlabStr = xLabSer.toString(tg, 'phones')
            def xlabFile = project.file("$destDir/${tgFile.name - '.TextGrid' + '.lab'}")
            xlabFile.text = xlabStr
        }
    }
}

package de.dfki.mary.voicebuilding.tasks

import org.gradle.api.*
import org.gradle.api.tasks.*

import org.m2ci.msp.jtgt.TextGrid
import org.m2ci.msp.jtgt.io.TextGridSerializer
//import org.m2ci.msp.jtgt.io.XWaveLabelSerializer

class ConvertTextGridToXLab extends DefaultTask {

    @InputDirectory
    File tgDir

    @OutputDirectory
    File destDir = project.file("$project.buildDir/xlab")

    @TaskAction
    void convert() {
        project.fileTree("$tgDir/data").include('*.TextGrid').collect { tgFile ->
            def xlabFile = project.file("$destDir/${tgFile.name - '.TextGrid' + '.xlab'}")

            //replace Strings (silence) that cannot be processed by MaryTTS
            def tgString= tgFile.text
            tgString = tgString.replaceAll( "text = \"sil\"", "text = \"_\"")
            tgString = tgString.replaceAll( "text = \"\"", "text = \"_\"")

            TextGridSerializer tgSer = new TextGridSerializer();
            TextGrid tg = tgSer.fromString(tgString)

            xlabFile.text = tgSer.toString(tg)
            //XWaveLabelSerializer xlabSer = new XWaveLabelSerializer();
            //xlabFile.text = xlabSer.toString(tg, "phones")

        }
    }
}
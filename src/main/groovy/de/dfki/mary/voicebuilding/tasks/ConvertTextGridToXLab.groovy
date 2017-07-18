package de.dfki.mary.voicebuilding.tasks

import org.gradle.api.*
import org.gradle.api.tasks.*

import org.m2ci.msp.jtgt.io.TextGridSerializer
import org.m2ci.msp.jtgt.io.XWaveLabelSerializer
import org.m2ci.msp.jtgt.Tier
import org.m2ci.msp.jtgt.TextGrid
import org.m2ci.msp.jtgt.Annotation

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
            tg = replaceSilenceString(tg)

            def xlabStr = xLabSer.toString(tg, 'phones')

            def xlabFile = project.file("$destDir/${tgFile.name - '.TextGrid' + '.lab'}")
            xlabFile.text = xlabStr
        }
    }

    TextGrid replaceSilenceString(TextGrid tg) {
        ArrayList<Tier> tiers = tg.getTiers()
        ArrayList<Tier> newTiers = new ArrayList<Tier>()
        tg.setTiers(newTiers)
        for (tier in tiers) {
            if (tier.getName().equals("phones")) {
                ArrayList<Annotation> anno = tier.getAnnotations()
                ArrayList<Annotation> newAnno = new ArrayList<Annotation>()
                tier.setAnnotations(newAnno)
                for (a in anno) {
                    if (a.getText().equals("sil") || a.getText().equals("")) {
                        a.setText("_")
                        tier.addAnnotation(a)
                    }
                    else {
                        tier.addAnnotation(a)
                    }
                }
            }
            tg.addTier(tier)
        }
        return tg
    }
}

package de.dfki.mary.voicebuilding

import org.gradle.testkit.runner.GradleRunner
import org.m2ci.msp.jtgt.io.XWaveLabelSerializer
import org.testng.annotations.*

import static org.gradle.testkit.runner.TaskOutcome.SUCCESS

class MaryttsKaldiMfaPluginFunctionalTest {

    GradleRunner gradle

    @BeforeSuite
    void setup() {
        def projectDir = File.createTempDir()
        gradle = GradleRunner.create().withProjectDir(projectDir).withPluginClasspath()
        new File(projectDir, 'build.gradle').withWriter {
            it << this.class.getResourceAsStream('build.gradle')
        }
    }

    @Test
    void canApplyPlugin() {
        def result = gradle.withArguments().build()
        assert true
    }

    @Test
    void testConvertTextToMaryXml() {
        def result = gradle.withArguments('convertTextToMaryXml').build()
        println result.output
    }

    @Test
    void testConvertTextGridToXLab() {
        def textGridFile = new File("$gradle.projectDir/build/TextGrid/data", 'test.TextGrid')
        textGridFile.parentFile.mkdirs()
        textGridFile.withWriter {
            it << this.class.getResourceAsStream('test.TextGrid')
        }
        def result = gradle.withArguments('convertTextGridToXLab').build()
        println result.output
        def expectedStr = this.class.getResourceAsStream('expected.lab').text
        def expected = new XWaveLabelSerializer().fromString(expectedStr)
        def actualStr = new File("$gradle.projectDir/build/lab/test.lab").text
        def actual = new XWaveLabelSerializer().fromString(actualStr)
        assert expected == actual
    }
}

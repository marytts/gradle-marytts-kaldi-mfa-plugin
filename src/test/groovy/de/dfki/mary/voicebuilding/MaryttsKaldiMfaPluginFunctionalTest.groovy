import org.gradle.testkit.runner.GradleRunner
import org.testng.annotations.BeforeSuite
import org.testng.annotations.Test

import static org.gradle.testkit.runner.TaskOutcome.SUCCESS

@Test
class MaryttsKaldiMfaPluginFunctionalTest {

    GradleRunner gradle

    @BeforeSuite
    void setup() {
        def projectDir = File.createTempDir()
        gradle = GradleRunner.create().withProjectDir(projectDir).withPluginClasspath()
        new File(projectDir, 'build.gradle').withWriter { buildscript ->
            buildscript.println 'plugins {'
            buildscript.println "  id 'marytts-kaldi-mfa'"
            buildscript.println '}'
            buildscript.println 'convertTextToMaryXml {'
            buildscript.println '  srcDir = file("text")'
            buildscript.println '  destDir = file("\$buildDir/maryXml")'
            buildscript.println '}'
        }
        def textFile = new File("$projectDir/text/fnord.txt")
        textFile.parentFile.mkdirs()
        textFile.text = "Fnord."
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
}

[![Build Status](https://travis-ci.org/marytts/gradle-marytts-kaldi-mfa-plugin.svg?branch=master)](https://travis-ci.org/marytts/gradle-marytts-kaldi-mfa-plugin)

# gradle-marytts-kaldi-mfa-plugin
this plugin uses the newest [release](https://github.com/MontrealCorpusTools/Montreal-Forced-Aligner/releases) of the Kaldi-based [Montreal Forced Aligner](https://montrealcorpustools.github.io/Montreal-Forced-Aligner/)

:::info
If you are on a linux system you have to install a library which is missing in the linux-release from [MFA](https://github.com/MontrealCorpusTools/Montreal-Forced-Aligner/releases).
We hope that this bug is fixed in a future version. Until now you can resolve this by installing this library via:

```
sudo apt-get install libatlas3-base
```
:::

### prerequisites
- we recommend using **Gradle 3.5** with **Groovy 2.4.10**[1]
- **yourproject/build/wav** (with your .wav-files) and **yourproject/build/text** (with your corresponding .txt-files)
    - the .wav-files have to be downsampled to **16 kHz** (we recommend using [SoX](http://sox.sourceforge.net) for this)

## How to apply this plugin

Follow the instructions on the [gradle plugin website](https://plugins.gradle.org/plugin/de.dfki.mary.voicebuilding.marytts-kaldi-mfa)

## How to configure your project
add this to **gradle.properties**
```
group=de.dfki.mary
version=0.5.0-SNAPSHOT
```

add the following lines to your **build.gradle**
```
plugins {
    id 'groovy'
    id "de.dfki.mary.voicebuilding.marytts-kaldi-mfa" version "0.2.0"
}

 ```
 - per default your **srcDir** is set to  **yourproject/text**
 - you can override your default-directories for the tasks in your **build.gradle**
 ```
 convertTextToMaryXml {
     srcDir = file("$buildDir/text")
 }

 prepareForcedAlignment {
     wavDir =  file("$buildDir/wav")
 }
 ```

finally run
```
./gradlew runForcedAlignment
```
the resulting **TextGrids** will be in **build/TextGrid**

### Optional post-processing
in order to use these TextGrids for voicebuilding with MaryTTS you have to replace *sil* in your TextGrids and convert them to .lab-files.
This task depends on *runForcedAlignment* as the TextGrids are created there.

If you used the default directories, this is done simply with:
```
./gradlew convertTextGridToXLab
```
Otherwise you want to override the default directory in your **build.gradle**. You can also **override the labelMapping**.  
 ```
 convertTextGridToXLab {
     tgDir = file("$buildDir/yourTextGridDir")
     labelMapping = [sil: 'pau']
 }
 ```

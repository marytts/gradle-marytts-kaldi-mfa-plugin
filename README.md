[![Build Status](https://travis-ci.org/marytts/gradle-marytts-kaldi-mfa-plugin.svg?branch=master)](https://travis-ci.org/marytts/gradle-marytts-kaldi-mfa-plugin)

# gradle-marytts-kaldi-mfa-plugin
this plugin uses a [docker image](https://github.com/psibre/marytts-dockerfiles/tree/master/kaldi-mfa) of the Kaldi-based [Montreal Forced Aligner](https://montrealcorpustools.github.io/Montreal-Forced-Aligner/)

### prerequisites
- this plugin requires [docker](https://www.docker.com)
- we recommend using **Gradle 3.5** with **Groovy 2.4.10**[1] 
- **yourproject/build/wav** (with your .wav-files) and **yourproject/build/text** (with your corresponding .txt-files) 
    - the .wav-files have to be downsampled to **16 kHz** (we recommend using [SoX](http://sox.sourceforge.net) for this)

## How to apply this plugin

**initialize** this plugin as a submodule
```
git submodule add https://github.com/marytts/gradle-marytts-kaldi-mfa-plugin.git buildSrc/gradle-marytts-kaldi-mfa-plugin
```
add this to **buildSrc/build.gradle**
```
repositories {
    jcenter()
}

dependencies {
    compile group: 'org.codehaus.groovy.modules.http-builder', name: 'http-builder', version: '0.7'
    runtime project(':gradle-marytts-kaldi-mfa-plugin')
}
```
add this to **buildSrc/settings.gradle**
```
include 'gradle-marytts-kaldi-mfa-plugin'
```

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
}

apply plugin: 'marytts-kaldi-mfa'

convertTextToMaryXml {
    srcDir = file("$buildDir/text")
}

runForcedAlignment {
    dependsOn convertTextToMaryXml, prepareForcedAlignment
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
 


[1] if you are using a version with **Groovy 2.4.7** or lower you have to change this
```
configurations.all {
    resolutionStrategy {
        dependencySubstitution {
            force 'org.codehaus.groovy:groovy-all:2.4.10'
        }
    }
}
```
in **build.gradle**.

But really you can just use a 3.5 gradle wrapper.

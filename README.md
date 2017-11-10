[![License: LGPL v3](https://img.shields.io/badge/License-LGPL%20v3-blue.svg)](https://www.gnu.org/licenses/lgpl-3.0)
[![Build Status](https://travis-ci.org/marytts/gradle-marytts-kaldi-mfa-plugin.svg?branch=master)](https://travis-ci.org/marytts/gradle-marytts-kaldi-mfa-plugin)

# gradle-marytts-kaldi-mfa-plugin

This plugin uses the [Kaldi]-based [Montreal Forced Aligner] to phonetically segment audio data based on corresponding text files.
It uses [MaryTTS] to predict the text pronunciation.

## Prerequisites

In your project directory, place the audio and text files under your `build` directory like this:
```
build
├── text
│   ├── utt0001.txt
│   ├── utt0002.txt
│   ├── utt0003.txt
│   ├── utt0004.txt
│   └── utt0005.txt
└── wav
    ├── utt0001.wav
    ├── utt0002.wav
    ├── utt0003.wav
    ├── utt0004.wav
    └── utt0005.wav
```
The `wav` files have to be downsampled to **16 kHz** (we recommend using [SoX] for this).

### Linux note

If you are on a linux system, ensure that the `libcblas.so.3` file is on the library search path, since it is not bundled with the Linux release of [Montreal Forced Aligner].
For details, see the [installation notes](http://montreal-forced-aligner.readthedocs.io/en/latest/installation.html#linux).

We hope that this issue is resolved in a future release; until then you can install the missing library by running

```
sudo apt-get install libatlas3-base
```
on Debian-based distributions (such as Ubuntu).

## How to apply this plugin

Please see the instructions at <https://plugins.gradle.org/plugin/de.dfki.mary.voicebuilding.marytts-kaldi-mfa>

## How to configure your project

To customize the directories configured as input for the forced alignment, you can override them like this in the project's `build.gradle`:

```groovy
convertTextToMaryXml {
    srcDir = file("some/other/text/directory")
}
prepareForcedAlignment {
    wavDir =  file("$buildDir/wav")
}
```

## How to run the forced alignement

Run
```
./gradlew runForcedAlignment
```
after which the resulting TextGrid files will be under `build/TextGrid`.

### Optional post-processing

In order to use the TextGrids for voicebuilding with [MaryTTS] you have to replace the `sil` and `sp` labels with `_` and convert them to `lab` files in Xwaves format.

This can be done simply by running
```
./gradlew convertTextGridToXLab
```
Note that you can also *override* the label mapping:
```groovy
convertTextGridToXLab {
    labelMapping = [sil: 'pau', a: 'ah'] // etc.
}
```

#### Using *convertTextGridToXLab* alone
If you want to use `convertTextGridToXLab` alone you may want to override the default TextGrid-directory which is `build/TextGrid/forcedAlignment`:
```
convertTextGridToXLab.tgDir = file("$buildDir/TextGrid")
```

[Kaldi]: http://kaldi-asr.org/
[MaryTTS]: http://mary.dfki.de/
[Montreal Forced Aligner]: https://montrealcorpustools.github.io/Montreal-Forced-Aligner/
[SoX]: http://sox.sourceforge.net/

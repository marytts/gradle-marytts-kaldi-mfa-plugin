Gradle MaryTTS Kaldi MFA Plugin
===============================

[Unreleased]
------------

### Changes
- updated jtgt to v5.3
- removed dependency in `convertTextGridToXlab.groovy`
    - this task can now also be used for other TextGrids
- updated jtgt to stable release
- updated the documentation
- added `labelMapping`
- adding missing package name
- generalized paths in Groovy task
- no requirement for Docker anymore
    - we directly use the binaries from MFA v1.0.0 now
    - check for OS to see which binary to download

[v0.1.0] (2017-07-14)
---------------------

### Initial release

- Forced Alignment based on a [Kaldi MFA Docker image](https://hub.docker.com/r/psibre/kaldi-mfa/)

[Unreleased]: https://github.com/marytts/gradle-marytts-kaldi-mfa-plugin/compare/v0.1.0...HEAD
[v0.1.0]: https://github.com/marytts/gradle-marytts-kaldi-mfa-plugin/releases/tag/v0.1.0

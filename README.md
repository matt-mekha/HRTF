# HRTF
A simple Head-Related Transfer Function implementation in Kotlin.

## Details
* Frequency attenuation based on three variables: frequency, which ear, and direction.
* Distance attenuation following the inverse square law.
* Interaural time differences to simulate the speed of sound.

## Features
* Maximum of 1024 sample latency (about 22ms excluding any OS delays).
* Can attenuate frequencies of up to 22050 Hz.
* SOFA file support for HRTF decoding.
* Wave and MP3 file support for audio testing.

## Try it yourself
Download the JAR from **Releases** and run the following command in the folder it's in:
```
java -jar HRTF.jar
```
Select a SOFA file (see the **res** folder or [this database](http://sofacoustics.org/data/database/) for more some examples)
Select an audio file (see the **res** folder for some samples).
Press **Run Demo** and listen with headphones!

# HRTF
A simple [Head-Related Transfer Function](https://en.wikipedia.org/wiki/Head-related_transfer_function) implementation in Kotlin that allows you to hear sounds with direction and elevation just using stereo headphones.

## Details
* Frequency attenuation based on three variables: frequency, which ear, azimuth, and elevation.
* Distance attenuation following the inverse square law.
* Interaural time differences to simulate the speed of sound.

## Features
* Maximum of 1024 sample latency (about 22ms excluding any OS delays).
* Can attenuate frequencies of up to 22050 Hz.
* SOFA file support for HRTF decoding.
* Wave and MP3 file support for audio testing.

## Try it yourself
1. Download [this JAR](https://github.com/matt-mekha/HRTF/releases/download/v0.1.0/HRTF.jar) (check for latest in [Releases](https://github.com/matt-mekha/HRTF/releases/latest)) and run the following command in the folder it's in:
```
java -jar HRTF.jar
```
2. Select a SOFA file (see the **test-resources** folder or [this database](http://sofacoustics.org/data/database/) for examples).
3. Select an audio file (see the **test-resources** folder for examples).
4. Press **Run Demo** and listen with headphones!.

# HRTF Demo
A simple [Head-Related Transfer Function](https://en.wikipedia.org/wiki/Head-related_transfer_function) demo made in Kotlin that allows you to hear sounds with direction and elevation just using stereo headphones.

## Details
* Frequency scaling based on the following variables: frequency, which ear, azimuth, and elevation.
* Distance attenuation following the inverse square law.
* Interaural time differences to simulate the speed of sound.

## Features
* Maximum of 1024 sample latency (about 22ms excluding any OS delays).
* Can process frequencies of up to 22050 Hz.
* SOFA file support for HRTF decoding.
* Wave and MP3 file support for audio testing.

## Try it yourself
1. Download [this JAR](https://github.com/matt-mekha/HRTF/releases/download/v0.1.0/HRTF.jar) (from the [latest release](https://github.com/matt-mekha/HRTF/releases/latest)) and run the following command in the folder it's in:
```
java -jar HRTF.jar
```
2. Select a SOFA file (see this repo's [test-resources/HRTF](test-resources/HRTF) folder or [this database](http://sofacoustics.org/data/database/) for examples).
3. Select an audio file (see this repo's [test-resources](test-resources) folder for examples).
4. Press **Run Demo** and listen with headphones!

## Credits
* [NetCDF Java](https://www.unidata.ucar.edu/software/netcdf-java/) for decoding the SOFA files.
* [Badlogic Audio Analysis](https://github.com/Uriopass/audio-analysis) for the [Fast Fourier Transform](https://en.wikipedia.org/wiki/Fast_Fourier_transform) implementation in Java.

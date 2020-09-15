package matt.mekha.hrtf.test

import com.badlogic.audio.io.AudioDevice
import matt.mekha.hrtf.HeadRelatedTransferFunction
import matt.mekha.hrtf.HrtfLocalizedAudioPlayer
import matt.mekha.hrtf.SphericalCoordinates
import matt.mekha.hrtf.WaveDecoder2
import org.junit.Test

class Tests {

    @Test
    fun audioTest() {
        val audioSource = WaveDecoder2("res/Waves.wav", 2)
        val audioDevice = AudioDevice(audioSource.sampleRate)
        val samples = FloatArray(1024)

        while (audioSource.readSamples(samples) > 0) {
            audioDevice.writeSamples(samples)
        }
    }

    @Test
    fun stereoTest() {
        val audioSource = WaveDecoder2("res/Waves.wav", 2)
        val audioDevice = AudioDevice(audioSource.sampleRate)
        val samples = FloatArray(1024)

        while (audioSource.readSamples(samples) > 0) {
            audioDevice.writeSamples(samples, FloatArray(1024))
        }
    }

    @Test
    fun hrtfTest() {
        val hrtf = HeadRelatedTransferFunction(sofaFilePath = "res/HRTF/IRC_1003.sofa")
        val audioSource = WaveDecoder2("res/Waves.wav", 2)
        val audioDevice = AudioDevice(audioSource.sampleRate)
        val player = HrtfLocalizedAudioPlayer(hrtf, audioSource, audioDevice, logToCsv = true)

        player.playAsync()

        var azimuth = 0.0
        while(player.isPlaying) {
            player.sphericalCoordinates = SphericalCoordinates(azimuth, 0.0, 1.4)
            //println(player.sphericalCoordinates)
            azimuth = (azimuth + 15) % 360
            Thread.sleep(250)
        }

        player.saveCsv()
    }
}
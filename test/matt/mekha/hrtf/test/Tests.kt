package matt.mekha.hrtf.test

import com.badlogic.audio.io.AudioDevice
import com.badlogic.audio.io.Decoder
import matt.mekha.hrtf.HeadRelatedTransferFunction
import matt.mekha.hrtf.HrtfLocalizedAudioPlayer
import matt.mekha.hrtf.SphericalCoordinates
import matt.mekha.hrtf.WaveDecoder2
import org.junit.Test
import kotlin.math.round
import kotlin.random.Random

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

    private fun circularMotion(audioSource: Decoder, elevation: Double = 0.0) {
        val hrtf = HeadRelatedTransferFunction(sofaFilePath = "res/HRTF/MIT_KEMAR.sofa")
        val audioDevice = AudioDevice(audioSource.sampleRate)
        val player = HrtfLocalizedAudioPlayer(hrtf, audioSource, audioDevice, logToCsv = true)

        player.playAsync()

        var azimuth = 0.0
        val azimuthIncrement = Random.nextBits(1) * 30.0 - 15.0
        while(player.isPlaying) {
            player.sphericalCoordinates = SphericalCoordinates(azimuth, elevation, 2.0)
            println(player.sphericalCoordinates)

            azimuth = (azimuth + azimuthIncrement) % 360

            Thread.sleep(400)
        }

        player.saveCsv()
    }

    @Test
    fun hrtfWavesCircularMotion() {
        circularMotion(WaveDecoder2("res/Waves.wav", 2))
    }

    @Test
    fun hrtfFootstepsCircularMotion() {
        circularMotion(WaveDecoder2("res/Footsteps.wav", 2), -45.0)
    }

    @Test
    fun hrtfDogCircularMotion() {
        circularMotion(WaveDecoder2("res/Dog.wav", 2))
    }

    @Test
    fun hrtfGunshotsCircularMotion() {
        circularMotion(WaveDecoder2("res/Gunshots.wav", 2), 0.0)
    }

    @Test
    fun hrtfGunshotsRandom() {
        val hrtf = HeadRelatedTransferFunction(sofaFilePath = "res/HRTF/MIT_KEMAR.sofa")
        val audioSource = WaveDecoder2("res/Gunshots.wav", 2)
        val audioDevice = AudioDevice(audioSource.sampleRate)
        val player = HrtfLocalizedAudioPlayer(hrtf, audioSource, audioDevice, logToCsv = true)

        player.playAsync()

        while(player.isPlaying) {
            player.sphericalCoordinates = SphericalCoordinates(round(Random.nextDouble() * 360.0), 0.0, 2.0)
            println(player.sphericalCoordinates)

            Thread.sleep(4000)
        }

        player.saveCsv()
    }
}
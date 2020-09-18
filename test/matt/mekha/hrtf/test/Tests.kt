package matt.mekha.hrtf.test

import com.badlogic.audio.io.AudioDevice
import com.badlogic.audio.io.Decoder
import matt.mekha.hrtf.*
import matt.mekha.hrtf.util.WaveDecoder2
import org.junit.Test
import javax.swing.JFrame
import kotlin.math.round
import kotlin.random.Random

class Tests {

    @Test
    fun audioTest() {
        val audioSource = WaveDecoder2("test-resources/Waves.wav")
        val audioDevice = AudioDevice(audioSource.sampleRate)
        val samples = FloatArray(1024)

        while (audioSource.readSamples(samples) > 0) {
            audioDevice.writeSamples(samples)
        }
    }

    @Test
    fun stereoTest() {
        val audioSource = WaveDecoder2("test-resources/Waves.wav")
        val audioDevice = AudioDevice(audioSource.sampleRate)
        val samples = FloatArray(1024)

        while (audioSource.readSamples(samples) > 0) {
            audioDevice.writeSamples(samples, FloatArray(1024))
        }
    }

    private fun circularMotion(audioSource: Decoder, elevation: Double = 0.0) {
        val hrtf = HeadRelatedTransferFunction(sofaFilePath = "test-resources/HRTF/IRC_1003.sofa")
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
        circularMotion(WaveDecoder2("Waves.wav"))
    }

    @Test
    fun hrtfBirdsCircularMotion() {
        circularMotion(WaveDecoder2("test-resources/Birds.wav"))
    }

    @Test
    fun hrtfFootstepsCircularMotion() {
        circularMotion(WaveDecoder2("test-resources/Footsteps.wav"), -45.0)
    }

    @Test
    fun hrtfDogCircularMotion() {
        circularMotion(WaveDecoder2("test-resources/Dog.wav"))
    }

    @Test
    fun hrtfGunshotsCircularMotion() {
        circularMotion(WaveDecoder2("test-resources/Gunshots.wav"), 0.0)
    }

    @Test
    fun hrtfGunshotsRandom() {
        val hrtf = HeadRelatedTransferFunction(sofaFilePath = "test-resources/HRTF/IRC_1003.sofa")
        val audioSource = WaveDecoder2("test-resources/Gunshots.wav")
        val audioDevice = AudioDevice(audioSource.sampleRate)
        val player = HrtfLocalizedAudioPlayer(hrtf, audioSource, audioDevice, logToCsv = true)

        player.playAsync()

        while(player.isPlaying) {
            player.sphericalCoordinates = SphericalCoordinates(round(Random.nextDouble() * 360.0), 0.0, 2.0)
            println(player.sphericalCoordinates)

            Thread.sleep(3500)
        }

        player.saveCsv()
    }

    @Test
    fun straightToDemo() {
        val window = JFrame("HRTF Demo")
        window.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        window.isResizable = false
        window.isVisible = true
        runDemo(window, WaveDecoder2("test-resources/Waves.wav"), "test-resources/HRTF/MIT_KEMAR.sofa")
    }

}
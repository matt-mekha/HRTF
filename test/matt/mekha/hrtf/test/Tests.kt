package matt.mekha.hrtf.test

import com.badlogic.audio.io.AudioDevice
import matt.mekha.hrtf.HeadRelatedTransferFunction
import matt.mekha.hrtf.HrtfLocalizedAudioPlayer
import matt.mekha.hrtf.SphericalCoordinates
import matt.mekha.hrtf.WaveDecoder2
import org.junit.Test

class Tests {

    @Test
    fun hrtfTest() {
        val hrtf = HeadRelatedTransferFunction(sofaFilePath = "HRTF/IRC_1003.sofa")
        val audioSource = WaveDecoder2("Birds.wav", 2)
        val audioDevice = AudioDevice()
        val player = HrtfLocalizedAudioPlayer(hrtf, audioSource, audioDevice, logToCsv = true)

        player.playAsync()

        var azimuth = 0.0
        while(player.isPlaying) {
            player.sphericalCoordinates = SphericalCoordinates(azimuth, 0.0, 5.0)
            println(player.sphericalCoordinates)
            azimuth = (azimuth + 15) % 360
            Thread.sleep(125)
        }

        player.saveCsv()
    }
}
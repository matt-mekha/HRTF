package matt.mekha.hrtf

import com.badlogic.audio.io.AudioDevice
import kotlin.random.Random

fun go() {
    val hrtf = HeadRelatedTransferFunction(sofaFilePath = "HRTF/MIT_KEMAR.sofa")
    val audioSource = WaveDecoder2("Waves.wav", 2)
    val audioDevice = AudioDevice(audioSource.sampleRate)
    val player = HrtfLocalizedAudioPlayer(hrtf, audioSource, audioDevice)

    player.playAsync()

    var azimuth = 0.0
    val azimuthIncrement = Random.nextBits(1) * 30.0 - 15.0
    while (player.isPlaying) {
        player.sphericalCoordinates = SphericalCoordinates(azimuth, 0.0, 2.0)
        println(player.sphericalCoordinates)

        azimuth = (azimuth + azimuthIncrement) % 360

        Thread.sleep(400)
    }
}
package matt.mekha.hrtf

import com.badlogic.audio.io.Decoder
import java.io.File
import java.io.PrintWriter
import java.lang.IllegalStateException
import java.nio.ByteBuffer
import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioInputStream
import javax.sound.sampled.AudioSystem

class WaveDecoder2(filePath: String, private val bytesPerSample: Int = 1) : Decoder {

    private val audioFormatDecoded: AudioFormat
    private val audioInputStreamDecoded: AudioInputStream

    init {
        val audioInputStreamEncoded: AudioInputStream = AudioSystem.getAudioInputStream(File(filePath))
        val audioFormatEncoded = audioInputStreamEncoded.format
        audioFormatDecoded = AudioFormat(
                AudioFormat.Encoding.PCM_SIGNED,
                audioFormatEncoded.sampleRate,
                bytesPerSample * 8,
                1,
                bytesPerSample,
                audioFormatEncoded.sampleRate,
                true
        )
        audioInputStreamDecoded = AudioSystem.getAudioInputStream(
                audioFormatDecoded,
                audioInputStreamEncoded
        )
    }

    override fun getSampleRate(): Int = audioFormatDecoded.sampleRate.toInt()

    override fun readSamples(samples: FloatArray?): Int {
        samples!!

        for(i in samples.indices) {
            samples[i] = when (bytesPerSample) {
                1 -> {
                    val byte = audioInputStreamDecoded.read()
                    if(byte == -1) return i
                    byte.toFloat() / 128.0f - 1.0f
                }
                2 -> {
                    val bytes = audioInputStreamDecoded.readNBytes(2)
                    if(bytes.size < 2) return i
                    ByteBuffer.wrap(bytes).short.toFloat() / 32768.0f
                }
                else -> {
                    throw IllegalStateException("Can't read this audio format (${audioFormatDecoded.frameSize} bytes per frame)!")
                }
            }
        }

        return samples.size
    }
}

//fun loadAudioFile(filePath: String, bytesPerSample: Int = 1) : AudioSource {
//    val audioInputStreamEncoded: AudioInputStream = AudioSystem.getAudioInputStream(File(filePath))
//    val audioFormatEncoded = audioInputStreamEncoded.format
//    val audioFormatDecoded = AudioFormat(
//            AudioFormat.Encoding.PCM_SIGNED,
//            audioFormatEncoded.sampleRate,
//            bytesPerSample * 8,
//            1,
//            bytesPerSample,
//            audioFormatEncoded.sampleRate,
//            true
//    )
//    val audioInputStreamDecoded = AudioSystem.getAudioInputStream(
//            audioFormatDecoded,
//            audioInputStreamEncoded
//    )
//
//    return AudioSource(
//            audioInputStreamDecoded.frameLength.toDouble() / audioFormatDecoded.frameRate,
//            audioFormatDecoded.sampleRate.toInt(),
//            {
//                when (bytesPerSample) {
//                    1 -> audioInputStreamDecoded.read().toDouble() / 128.0 - 1.0
//                    2 -> ByteBuffer.wrap(audioInputStreamDecoded.readNBytes(bytesPerSample)).short.toDouble() / 32768.0
//                    else -> 0.0
//                }
//            },
//            {
//                audioInputStreamDecoded.close()
//                audioInputStreamEncoded.close()
//            }
//    )
//}

fun saveCsv(filePath: String, data: Iterable<Iterable<*>>) {
    val file = File(filePath)

    val pw = PrintWriter(file)
    for(row in data) {
        pw.println(row.joinToString(","))
    }
    pw.close()
}
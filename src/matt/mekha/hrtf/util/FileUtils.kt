package matt.mekha.hrtf.util

import com.badlogic.audio.io.Decoder
import com.badlogic.audio.io.MP3Decoder
import java.io.File
import java.io.InputStream
import java.io.PrintWriter
import java.lang.IllegalArgumentException
import java.lang.IllegalStateException
import java.nio.ByteBuffer
import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioInputStream
import javax.sound.sampled.AudioSystem

fun getInternalFile(filePath: String): InputStream = Thread.currentThread().contextClassLoader.getResourceAsStream(filePath)!!

class WaveDecoder2(filePath: String, private val bytesPerSample: Int = 2) : Decoder {

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

fun decodeAudioFile(file: File) : Decoder {
    return when(file.extension) {
        "wav" -> {
            WaveDecoder2(file.path)
        }
        "mp3" -> {
            MP3Decoder(file.inputStream())
        }
        else -> {
            throw IllegalArgumentException("Unrecognized audio file extension.")
        }
    }
}

fun saveCsv(filePath: String, data: Iterable<Iterable<*>>) {
    val file = File(filePath)

    val pw = PrintWriter(file)
    for(row in data) {
        pw.println(row.joinToString(","))
    }
    pw.close()
}
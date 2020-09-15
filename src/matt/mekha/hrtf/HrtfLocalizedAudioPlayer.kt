package matt.mekha.hrtf

import com.badlogic.audio.analysis.FFT
import com.badlogic.audio.io.AudioDevice
import com.badlogic.audio.io.Decoder
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.ArrayList
import kotlin.random.Random

class HrtfLocalizedAudioPlayer(
        private val hrtf: HeadRelatedTransferFunction,
        private val audioSource: Decoder,
        private val audioDevice: AudioDevice,
        private val logToCsv: Boolean = false
) {

    var sphericalCoordinates = SphericalCoordinates.forward

    private val sampleBufferSize = 1024
    private val sampleBufferDuration = sampleBufferSize.toDouble() / audioSource.sampleRate.toDouble()

    private val fft = FFT(sampleBufferSize, sampleBufferDuration.toFloat())

    var isPlaying = false

    private val data = ArrayList<ArrayList<*>>()

    init {
        if(logToCsv) {
            val headers = ArrayList<String>()
            headers.add("Sample")
            headers.add("Azimuth")
            headers.add("Elevation")
            headers.add("Distance")
            headers.add("Original")
            headers.add("Left")
            headers.add("Right")
            data.add(headers)
        }
    }

    fun playSync() {
        isPlaying = true
        while(isPlaying) {
            everyXSamples()
        }
    }

    fun playAsync() {
        isPlaying = true
        Thread(this::playSync).start()
    }

    fun stop() {
        isPlaying = false
    }

    fun saveCsv() {
        if(logToCsv) {
            val time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy_MM_dd_HH_mm_ss"))
            synchronized(data) {
                saveCsv("out/HRTF_Localization_Data_$time.csv", data)
            }
        } else {
            throw IllegalStateException("Log to CSV option was false, so no data is available to save.")
        }
    }

    private fun everyXSamples() {
        val sampleBuffer = FloatArray(sampleBufferSize)
        val samplesRead = audioSource.readSamples(sampleBuffer)
        if(samplesRead < sampleBufferSize) stop()

        val earSamples = EnumMap<Ear, FloatArray>(Ear::class.java)
        for(ear in Ear.values()) {
            fft.forward(sampleBuffer)
            for(i in sampleBuffer.copyOfRange(0, sampleBufferSize/2).indices) {
                if(i==0) continue
                val frequency = i.toDouble() / sampleBufferDuration
                val transformation = hrtf.transfer(
                        frequency,
                        sphericalCoordinates,
                        ear
                )
                //if(transformation.amplitude > 1.0) println(transformation.amplitude)
                fft.scaleBand(i, transformation.amplitude.toFloat())
            }
            earSamples[ear] = FloatArray(sampleBufferSize)
            fft.inverse(earSamples[ear])

            // TODO implement delay
        }

        audioDevice.writeSamples(
                earSamples[Ear.LEFT],
                earSamples[Ear.RIGHT]
        )

        if(logToCsv) {
            synchronized(data) {
                for ((i, sample) in sampleBuffer.withIndex()) {
                    val dataRow = ArrayList<Double>()
                    dataRow.add(data.size.toDouble())
                    dataRow.add(sphericalCoordinates.azimuth)
                    dataRow.add(sphericalCoordinates.elevation)
                    dataRow.add(sphericalCoordinates.radius)
                    dataRow.add(sample.toDouble())
                    dataRow.add(earSamples[Ear.LEFT]!![i].toDouble())
                    dataRow.add(earSamples[Ear.RIGHT]!![i].toDouble())
                    data.add(dataRow)
                }
            }
        }
    }

}
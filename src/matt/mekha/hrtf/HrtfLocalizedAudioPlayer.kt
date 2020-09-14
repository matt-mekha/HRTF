package matt.mekha.hrtf

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.cos

class HrtfLocalizedAudioPlayer(private val hrtf: HeadRelatedTransferFunction, private val audioSource: AudioSource, private val logToCsv: Boolean = false) {

    var sphericalCoordinates = SphericalCoordinates.forward

    private val sampleWindowDuration = 0.1
    private val sampleWindowWidth = (sampleWindowDuration * audioSource.sampleRate).toInt()
    private val frequencies = List(500) { ((it + 1) * 20).toDouble() }
    private val sampleTime = (1000000000.0 / audioSource.sampleRate.toDouble()).toLong()

    private val rdft: RollingDiscreteFourierTransform

    var isPlaying = false

    private val data = ArrayList<ArrayList<*>>()

    init {
        rdft = RollingDiscreteFourierTransform(
                audioSource.sampleFunction,
                sampleWindowWidth,
                sampleWindowDuration,
                frequencies
        )

        if(logToCsv) {
            val headers = ArrayList<String>()
            headers.add("Azimuth")
            headers.add("Elevation")
            headers.add("Distance")
            headers.add("Original")
            headers.add("Left")
            headers.add("Right")
            data.add(headers)
        }
    }

    fun play() {
        rdft.prepare()

        Thread {
            isPlaying = true
            while(isPlaying) {
                val startTime = System.nanoTime()
                everySample()
                val elapsedTime = System.nanoTime() - startTime

                if(sampleTime - elapsedTime < 0) {
                    //println("Max duration: $sampleTime, Actual: $elapsedTime")
                } else {
                    Thread.sleep(sampleTime - elapsedTime)
                }
            }
        }.start()
    }

    fun stop() {
        isPlaying = false
    }

    fun saveCsv() {
        if(logToCsv) {
            val time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy_MM_dd_HH_mm_ss"))
            synchronized(data) {
                saveCsv("HRTF/HRTF_Localization_Data_$time.csv", data)
            }
        } else {
            throw IllegalStateException("Log to CSV option was false, so no data is available to save.")
        }
    }

    private fun everySample() {
        rdft.roll()

        val dataRow = ArrayList<Double>()
        if(logToCsv) {
            dataRow.add(sphericalCoordinates.azimuth)
            dataRow.add(sphericalCoordinates.elevation)
            dataRow.add(sphericalCoordinates.radius)
            dataRow.add(rdft.latestSample)
        }

        for(ear in Ear.values()) {
            val cdft = ComplexDiscreteFourierTransform(
                    {
                        val frequency = frequencies[it]
                        val rawAmplitude = rdft.getFrequencyAmplitude(frequencies[it])
                        val transformation = hrtf.transfer(
                                frequency,
                                rawAmplitude.magnitude,
                                sphericalCoordinates,
                                ear
                        )
                        val processedAmplitude = rawAmplitude * transformation.amplitude
                        // TODO do something with transformation.delay

                        rawAmplitude // TODO change back to processedAmplitude
                    },
                    frequencies.size,
                    1.0,
                    true
            )

            var localizedSample = 0.0//cdft.getFrequencyAmplitude(0.0).magnitude
            for(frequency in frequencies) {
                val amplitude = cdft.getFrequencyAmplitude(frequency)
                localizedSample += amplitude.magnitude * cos(frequency * amplitude.theta)
            }
            localizedSample *= audioSource.sampleRate

            // TODO output this value to corresponding ear

            if(logToCsv) dataRow.add(localizedSample)
        }

        if(logToCsv) {
            synchronized(data) {
                data.add(dataRow)
            }
        }
    }

}
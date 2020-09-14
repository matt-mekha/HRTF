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

    var isPlaying = false

    private val data = ArrayList<ArrayList<*>>()

    init {
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
        // TODO shift buffer

        val dataRow = ArrayList<Double>()
        if(logToCsv) {
            dataRow.add(sphericalCoordinates.azimuth)
            dataRow.add(sphericalCoordinates.elevation)
            dataRow.add(sphericalCoordinates.radius)
            dataRow.add(rdft.latestSample)
        }

        for(ear in Ear.values()) {
            // TODO FFT on buffer
            // TODO HRTF adjustments
            // TODO IFFT on buffer

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
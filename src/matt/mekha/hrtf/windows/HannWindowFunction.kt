package matt.mekha.hrtf.windows

import kotlin.math.PI
import kotlin.math.pow
import kotlin.math.sin

class HannWindowFunction(windowSize: Int) : WindowFunction(windowSize) {
    override fun apply(x: Double): Double {
        return sin(x * PI / windowSize).pow(2)
    }
}
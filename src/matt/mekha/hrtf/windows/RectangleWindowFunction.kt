package matt.mekha.hrtf.windows

class RectangleWindowFunction(windowSize: Int) : WindowFunction(windowSize) {
    override fun apply(x: Double): Double {
        return 1.0
    }
}
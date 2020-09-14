package matt.mekha.hrtf.windows

abstract class WindowFunction(protected val windowSize: Int) {
    abstract fun apply(x: Double) : Double
}
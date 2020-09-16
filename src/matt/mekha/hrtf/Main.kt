package matt.mekha.hrtf

import com.badlogic.audio.io.AudioDevice
import com.badlogic.audio.io.Decoder
import java.awt.BorderLayout
import java.awt.Dimension
import java.io.File
import javax.swing.*
import javax.swing.filechooser.FileNameExtensionFilter
import kotlin.random.Random

var sofaFile: File? = null
var audioFile: File? = null

const val padding = 20
const val spacing = 5

fun entryPoint() {
    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())

    val window = JFrame("HRTF Demo")
    window.size = Dimension(500, 500)
    window.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
    window.isResizable = false

    fun createFormPanel() {
        var formPanel = JPanel()
        formPanel.layout = BoxLayout(formPanel, BoxLayout.Y_AXIS)
        formPanel.border = BorderFactory.createEmptyBorder(padding, padding, padding, padding)

        val title = JLabel("HRTF Demo")
        formPanel.add(title)

        val submitButton = JButton("Start Demo")
        submitButton.isEnabled = false
        submitButton.addActionListener {
            formPanel.isVisible = false
            window.remove(formPanel)
            formPanel = JPanel()

            fun runDemo(audioSource: Decoder, sofaFilePath: String) {
                val hrtf = HeadRelatedTransferFunction(sofaFilePath)
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

                window.dispose()
                Thread(::entryPoint).start()
            }

            Thread { runDemo(decodeAudioFile(audioFile!!), sofaFile!!.path) }.start()
        }

        fun createFileChooser(fileFilter: FileNameExtensionFilter, buttonText: String, onSelect: (File) -> Unit) {
            val fileChooser = JFileChooser()
            fileChooser.fileFilter = fileFilter
            fileChooser.isAcceptAllFileFilterUsed = false
            fileChooser.currentDirectory = File(System.getProperty("user.dir"))

            val fileChooserPanel = JPanel()
            fileChooserPanel.layout = BoxLayout(fileChooserPanel, BoxLayout.X_AXIS)

            val fileChooserLabel = JLabel("")

            val fileChooserButton = JButton(buttonText)
            fileChooserButton.addActionListener {
                val returnVal = fileChooser.showOpenDialog(null)
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    fileChooserLabel.text = fileChooser.selectedFile.name
                    onSelect(fileChooser.selectedFile)
                }
                submitButton.isEnabled = (sofaFile != null && audioFile != null)
            }

            fileChooserPanel.add(fileChooserButton)
            fileChooserPanel.add(Box.createRigidArea(Dimension(spacing, 0)))
            fileChooserPanel.add(fileChooserLabel)
            formPanel.add(Box.createRigidArea(Dimension(0, spacing)))
            formPanel.add(fileChooserPanel)
        }

        createFileChooser(
                FileNameExtensionFilter("SOFA Files", "sofa"),
                "Select HRTF..."
        ) {
            sofaFile = it
        }

        createFileChooser(
                FileNameExtensionFilter("Audio Files", "wav", "mp3"),
                "Select Audio..."
        ) {
            audioFile = it
        }

        formPanel.add(Box.createRigidArea(Dimension(0, spacing)))
        formPanel.add(submitButton)

        window.contentPane.add(formPanel, BorderLayout.LINE_START)
    }

    createFormPanel()
    window.isVisible = true
}
package matt.mekha.hrtf

import com.badlogic.audio.io.AudioDevice
import com.badlogic.audio.io.Decoder
import matt.mekha.hrtf.util.ImagePanel
import matt.mekha.hrtf.util.decodeAudioFile
import java.awt.*
import java.io.File
import javax.swing.*
import javax.swing.filechooser.FileNameExtensionFilter
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.random.Random

var sofaFile: File? = null
var audioFile: File? = null

fun runDemo(window: JFrame, audioSource: Decoder, sofaFilePath: String) {
    val panel = JPanel()
    panel.layout = null

    val person = ImagePanel("Images/Person.jpg")
    person.bounds = Rectangle(180, 175, 120, 110)
    panel.add(person)

    val ripple = ImagePanel("Images/Ripple.png")
    panel.add(ripple)

    window.size = Dimension(500, 500)
    window.layout = BorderLayout()
    window.add(panel, BorderLayout.CENTER)
    window.revalidate()

    val hrtf = HeadRelatedTransferFunction(sofaFilePath)
    val audioDevice = AudioDevice(audioSource.sampleRate)
    val player = HrtfLocalizedAudioPlayer(hrtf, audioSource, audioDevice)

    player.playAsync()

    var azimuth = 0.0
    val azimuthIncrement = Random.nextBits(1) * 30.0 - 15.0

    val r = 150
    val s = 80

    while (player.isPlaying) {
        player.sphericalCoordinates = SphericalCoordinates(azimuth, 0.0, 2.0)

        val d = azimuth * PI / 180
        val x = -r * sin(d) - s/2 + window.size.width/2
        val y = -r * cos(d) - s/2 + window.size.height/2
        ripple.bounds = Rectangle(x.roundToInt(), y.roundToInt(), s, s)

        println(player.sphericalCoordinates)

        azimuth = (azimuth + azimuthIncrement) % 360

        Thread.sleep(400)
    }

    window.dispose()
    entryPoint()
}

fun entryPoint() {
    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())

    val window = JFrame("HRTF Demo")
    window.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
    window.isResizable = false

    fun createFormPanel() {
        var formPanel = JPanel()
        formPanel.size = window.size

        val layout = GridBagLayout()
        layout.columnWidths = intArrayOf(150, 150)
        formPanel.layout = layout

        val title = JLabel("HRTF Demo")
        title.horizontalAlignment = SwingConstants.CENTER
        title.font = Font("Arial", Font.BOLD, 20)
        var constraints = GridBagConstraints()
        constraints.gridx = 0
        constraints.gridy = 0
        constraints.gridwidth = 2
        constraints.gridheight = 1
        constraints.ipadx = 30
        constraints.ipady = 30
        constraints.fill = GridBagConstraints.HORIZONTAL
        formPanel.add(title, constraints)

        val submitButton = JButton("Start Demo")
        submitButton.font = Font("Arial", Font.BOLD, 14)
        submitButton.isEnabled = false
        submitButton.addActionListener {
            formPanel.isVisible = false
            window.remove(formPanel)
            formPanel = JPanel()

            Thread { runDemo(window, decodeAudioFile(audioFile!!), sofaFile!!.path) }.start()
        }

        fun createFileChooser(fileFilter: FileNameExtensionFilter, buttonText: String, y: Int, onSelect: (File) -> Unit) {
            val fileChooser = JFileChooser()
            fileChooser.fileFilter = fileFilter
            fileChooser.isAcceptAllFileFilterUsed = false
            fileChooser.currentDirectory = File(System.getProperty("user.dir"))

            val fileChooserLabel = JLabel("")
            fileChooserLabel.font = Font("Arial", Font.PLAIN, 14)
            fileChooserLabel.horizontalAlignment = SwingConstants.CENTER

            val fileChooserButton = JButton(buttonText)
            fileChooserButton.font = Font("Arial", Font.PLAIN, 14)
            fileChooserButton.addActionListener {
                val returnVal = fileChooser.showOpenDialog(null)
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    var truncatedName = fileChooser.selectedFile.name
                    if(truncatedName.length > 15) {
                        truncatedName = truncatedName.slice(0 until 15) + "..."
                    }
                    fileChooserLabel.text = truncatedName
                    onSelect(fileChooser.selectedFile)
                }
                submitButton.isEnabled = (sofaFile != null && audioFile != null)
            }

            constraints = GridBagConstraints()
            constraints.gridx = 0
            constraints.gridy = y
            constraints.gridwidth = 1
            constraints.gridheight = 1
            constraints.ipadx = 30
            constraints.ipady = 30
            constraints.fill = GridBagConstraints.HORIZONTAL
            formPanel.add(fileChooserButton, constraints)

            constraints = GridBagConstraints()
            constraints.gridx = 1
            constraints.gridy = y
            constraints.gridwidth = 1
            constraints.gridheight = 1
            constraints.ipadx = 30
            constraints.ipady = 30
            constraints.fill = GridBagConstraints.HORIZONTAL
            formPanel.add(fileChooserLabel, constraints)
        }

        createFileChooser(
                FileNameExtensionFilter("SOFA Files", "sofa"),
                "Select HRTF...", 1
        ) {
            sofaFile = it
        }

        createFileChooser(
                FileNameExtensionFilter("Audio Files", "wav", "mp3"),
                "Select Audio...", 2
        ) {
            audioFile = it
        }

        constraints = GridBagConstraints()
        constraints.gridx = 0
        constraints.gridy = 3
        constraints.gridwidth = 2
        constraints.gridheight = 1
        constraints.ipadx = 30
        constraints.ipady = 30
        constraints.fill = GridBagConstraints.HORIZONTAL
        formPanel.add(submitButton, constraints)

        window.contentPane.add(formPanel, BorderLayout.LINE_START)
    }

    createFormPanel()
    window.pack()
    window.isVisible = true
}
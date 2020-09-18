package matt.mekha.hrtf.util

import java.awt.Graphics
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import javax.swing.JPanel

class ImagePanel(filePath: String) : JPanel() {
    private val image: BufferedImage = ImageIO.read(getInternalFile(filePath))

    override fun paintComponent(g: Graphics) {
        super.paintComponent(g)
        g.drawImage(image, 0, 0, size.width, size.height, this)
    }
}
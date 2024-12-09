import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import java.io.File
import javax.imageio.ImageIO

fun loadImageFromFile(path: String): ImageBitmap? {
    return try {
        val file = File(path)
        val bufferedImage = ImageIO.read(file)
        bufferedImage?.let { it.toComposeImageBitmap() }
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

// before the start of te application, Program deletes all of the previos charts
fun deletePngFilesInImagesFolder() {
    val imagesFolder = File(System.getProperty("user.dir"), "images")
    imagesFolder.listFiles { _, name -> name.endsWith(".png", ignoreCase = true) }
        ?.forEach { it.delete() }
}

// return list of all virtual portfolios 
fun listOfPortfolios(): List<String> {
    val currentDir = System.getProperty("user.dir")
    val virtualFolder = File("$currentDir/virtual")
    if (virtualFolder.exists() && virtualFolder.isDirectory) {
        return virtualFolder.listFiles()?.filter { it.isDirectory }?.map { it.name } ?: emptyList()
    }
    return emptyList()
}
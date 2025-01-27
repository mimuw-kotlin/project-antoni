import java.io.File
import java.io.FileWriter
import java.io.IOException

fun getPreviousObservation(stock: Stock): String {
    val currentDir = System.getProperty("user.dir")
    val stockDir = "$currentDir/observations/${stock.name}"
    val observationFile = File("$stockDir/observations.txt")

    return if (observationFile.exists()) {
        try {
            observationFile.readText()
        } catch (e: IOException) {
            "Error reading observations: ${e.message}"
        }
    } else {
        "No observations found for stock: ${stock.name}"
    }
}

fun newObservation(stock: Stock, observation: String) {
    val currentDir = System.getProperty("user.dir")
    val stockDir = "$currentDir/observations/${stock.name}"

    val dir = File(stockDir)
    if (!dir.exists()) {
        dir.mkdirs()
    }

    val observationFile = File("$stockDir/observations.txt")
    try {
        FileWriter(observationFile, true).use { writer ->
            writer.write("$observation\n")
        }
    } catch (e: IOException) {
        println("An error occurred while writing the observation: ${e.message}")
    }
}

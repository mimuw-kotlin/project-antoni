import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application


fun main() = application {
    deletePngFilesInImagesFolder()
    Window(
        onCloseRequest = ::exitApplication,
        title = "Desktop Application",
        state = WindowState(width = 1280.dp, height = 720.dp)
    ) {
        App()
    }
}

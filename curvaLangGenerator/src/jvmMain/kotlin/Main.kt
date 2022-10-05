// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import java.awt.color.ColorSpace.TYPE_RGB
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

object Images {

    val dec = "dec.png"
    val decPointer = "decPointer.png"
    val gotoLabel = "gotoLabel.png"
    val `if` = "if.png"
    val inc = "inc.png"
    val incPointer = "incPointer.png"
    val read = "read.png"
    val whileBegin = "whileBegin.png"
    val whileEnd = "whileEnd.png"
    val write = "write.png"

    val brainFuckCharToImage = mapOf(
        '>' to incPointer,
        '<' to decPointer,
        '+' to inc,
        '-' to dec,
        '.' to write,
        ',' to read,
        '[' to whileBegin,
        ']' to whileEnd,
    )
        .mapValues { (_, v) -> ImageIO.read(Images.javaClass.classLoader.getResourceAsStream(v))!! }

    val imageSize = 15 to 15
}

fun generateImage(text: String): BufferedImage {
    val result = BufferedImage(Images.imageSize.first, Images.imageSize.second * text.length, TYPE_RGB)

    val offsetX = 0
    var offsetY = 0
    val graphics = result.graphics
    text.forEach {
        val currentElement = Images.brainFuckCharToImage.getValue(it)
        graphics.drawImage(currentElement, offsetX, offsetY, null)
        offsetY += Images.imageSize.second
    }

    result.flush()
    ImageIO.write(result, "PNG", File("lastGenerated.png"))
    return result
}

@Composable
@Preview
fun App() {
    val helloWorld =
        "++++++++[>++++[>++>+++>+++>+<<<<-]>+>+>->>+[<]<-]>>.>---.+++++++..+++.>>.<-.<.+++.------.--------.>>+.>++."

    var text by remember { mutableStateOf(helloWorld) }
    var lastImage by remember { mutableStateOf<BufferedImage?>(null) }

    MaterialTheme {
        LazyColumn(Modifier.padding(4.dp)) {
            item {
                TextField(text, onValueChange = { text = it })
                Spacer(Modifier.height(2.dp))
                Button(onClick = { lastImage = generateImage(text) }) {
                    Text("Curva!")
                }
            }
            lastImage?.let {
                item {
                    Spacer(Modifier.height(2.dp))
                    Image(it.toComposeImageBitmap(), "curvalang image")
                }
            }
        }
    }
}

fun main() = application {
    Window(onCloseRequest = ::exitApplication) {
        App()
    }
}

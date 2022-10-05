// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import java.awt.color.ColorSpace.TYPE_RGB
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import java.awt.Color as AwtColor

object Images {

    val dec = "dec.png"
    val decPointer = "decPointer.png"
    val gotoLabel = "gotoLabel.png"

    //    val `if` = "if.png"
    val `if` = "gotoLabel.png"  // todo: we draw loops currently from one side
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
        .mapValues { (_, v) -> v.resourceNameToImage() }

    val brainFuckCharToImageWithLoops = brainFuckCharToImage + mapOf(
        '[' to `if`,
        ']' to gotoLabel,
    )
        .mapValues { (_, v) -> v.resourceNameToImage() }

    val imageSize = 15 to 15

    val loopElementSize = 7 to 15

    val start = BufferedImage(imageSize.first, imageSize.second, TYPE_RGB).apply {
        val g = this.graphics
        g.color = AwtColor.WHITE
        g.fillRect(0, 0, this.width, this.height)

        g.color = AwtColor.BLACK
        g.fillRect(imageSize.first / 2 - 1, imageSize.second / 2 - 1, 3, 3)
        g.drawLine(imageSize.first / 2, imageSize.second / 2, imageSize.first / 2, imageSize.second)
    }

    private fun String.resourceNameToImage() = ImageIO.read(Images.javaClass.classLoader.getResourceAsStream(this))!!
}

fun generateColumnImage(
    text: String,
    charToImage: Map<Char, BufferedImage> = Images.brainFuckCharToImage
): BufferedImage {
    val result = BufferedImage(Images.imageSize.first, Images.imageSize.second * (text.length + 1), TYPE_RGB)

    val offsetX = 0
    var offsetY = 0
    val graphics = result.graphics
    graphics.drawImage(Images.start, offsetX, offsetY, null)
    offsetY += Images.imageSize.second

    text.forEach {
        val currentElement = charToImage.getValue(it)
        graphics.drawImage(currentElement, offsetX, offsetY, null)
        offsetY += Images.imageSize.second
    }

    return result
}

fun generateColumnImageWithLoops(text: String): BufferedImage {
    val imageWithoutBranches = generateColumnImage(text, Images.brainFuckCharToImageWithLoops)

    val whileRanges = mutableListOf<IntRange>()
    val pendingOpens = mutableListOf<Int>()

    text.forEachIndexed { i, c ->
        when (c) {
            '[' -> pendingOpens.add(i)
            ']' -> whileRanges.add(pendingOpens.removeLast()..i)
        }
    }

    fun IntRange.contains(other: IntRange) = this.start <= other.start && other.endInclusive <= this.endInclusive

    val stacksDepths = whileRanges.map { range -> whileRanges.count { it.contains(range) } }
    val maxStackDepth = stacksDepths.maxOrNull()!!

    val result = BufferedImage(
        Images.imageSize.first + maxStackDepth * Images.loopElementSize.first,
        Images.imageSize.second * (text.length + 1),
        TYPE_RGB
    )
    val g = result.graphics
    g.color = AwtColor.WHITE
    g.fillRect(0, 0, result.width, result.height)

    g.drawImage(imageWithoutBranches, 0, 0, null)

    whileRanges.forEachIndexed { i, range ->
        val level = maxStackDepth - stacksDepths[i]

        g.color = AwtColor.BLACK
        // horizontal begin
        g.drawLine(
            Images.imageSize.first,
            (1 + range.start) * Images.imageSize.second + Images.imageSize.second / 2,
            Images.imageSize.first + level * Images.loopElementSize.first + Images.loopElementSize.first / 2,
            (1 + range.start) * Images.imageSize.second + Images.imageSize.second / 2,
        )
        // main vertical line
        g.drawLine(
            Images.imageSize.first + level * Images.loopElementSize.first + Images.loopElementSize.first / 2,
            (1 + range.start) * Images.imageSize.second + Images.imageSize.second / 2,
            Images.imageSize.first + level * Images.loopElementSize.first + Images.loopElementSize.first / 2,
            (1 + range.endInclusive) * Images.imageSize.second + Images.imageSize.second / 2,
        )
        // horizontal end
        g.drawLine(
            Images.imageSize.first + level * Images.loopElementSize.first + Images.loopElementSize.first / 2,
            (1 + range.endInclusive) * Images.imageSize.second + Images.imageSize.second / 2,
            Images.imageSize.first,
            (1 + range.endInclusive) * Images.imageSize.second + Images.imageSize.second / 2,
        )
    }

    return result
}

fun generateAndSaveImage(text: String, loopsNeeded: Boolean): BufferedImage {
    val result = when (loopsNeeded) {
        true -> generateColumnImageWithLoops(text)
        false -> generateColumnImage(text)
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
    var loopsNeeded by remember { mutableStateOf(true) }

    MaterialTheme {
        LazyColumn(Modifier.fillMaxWidth().padding(4.dp)) {
            item {
                TextField(text, onValueChange = { text = it }, Modifier.fillMaxWidth())
                Spacer(Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Button(onClick = { lastImage = generateAndSaveImage(text, loopsNeeded) }) {
                        Text("Curva!")
                    }
                    Spacer(Modifier.width(2.dp))
                    Checkbox(loopsNeeded, onCheckedChange = { loopsNeeded = it })
                    Spacer(Modifier.width(1.dp))
                    Text("Visualize loops")
                }
            }
            lastImage?.let {
                item {
                    Spacer(Modifier.height(2.dp))
                    Image(it.toComposeImageBitmap(), "curvalang image", Modifier.border(1.dp, Color.Cyan))
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

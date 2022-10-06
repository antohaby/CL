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
import java.awt.Graphics2D
import java.awt.color.ColorSpace.TYPE_RGB
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import kotlin.math.PI
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

    val error = BufferedImage(350, 20, TYPE_RGB).apply {
        val g = this.graphics
        g.color = AwtColor.WHITE
        g.fillRect(0, 0, this.width, this.height)

        g.font = g.font.deriveFont(15f)
        g.color = AwtColor.RED
        g.drawString("Error... Check console, fix the input, and try again!", 5, 15)
    }

    val turnL = "turn_left.png".resourceNameToImage()
    val turnR = "turn_right.png".resourceNameToImage()

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

fun BufferedImage.cropToUsedSize(): BufferedImage {
    var minX = width
    var maxX = 0
    var minY = height
    var maxY = 0

    repeat(width) { x ->
        repeat(height) { y ->
            if (getRGB(x, y) == AwtColor.BLACK.rgb) {
                minX = minX.coerceAtMost(x)
                maxX = maxX.coerceAtLeast(x)
                minY = minY.coerceAtMost(y)
                maxY = maxY.coerceAtLeast(y)
            }
        }
    }

    val padding = 10

    minX -= padding
    minY -= padding
    maxX += padding
    maxY += padding

    val result = BufferedImage(maxX - minX, maxY - minY, TYPE_RGB)
    val g = result.graphics
    g.drawImage(this, -minX, -minY, null)
    g.dispose()
    return result
}

fun generateWithRotations(text: String, rotations: String): BufferedImage {
    val imageSize = 5000
    val result = BufferedImage(imageSize, imageSize, TYPE_RGB)
    val g = result.graphics

    g.color = AwtColor.WHITE
    g.fillRect(0, 0, result.width, result.height)

    var positionX = imageSize / 2
    var positionY = imageSize / 2

    val moves = rotations.split('-').map { it[0] to it.substring(1).toInt() }.toMutableList()
    moves[0] = moves[0].copy(first = ' ')  // don't change direction initially

    var currentElementId = 0
    var currentDirection = rotations[0]

    fun movePosition(direction: Char) {
        when (direction) {
            'N' -> positionY -= Images.imageSize.second
            'W' -> positionX -= Images.imageSize.first
            'S' -> positionY += Images.imageSize.second
            'E' -> positionX += Images.imageSize.first
        }
    }

    fun drawImageAndMovePosition(image: BufferedImage, drawDirection: Char, moveDirection: Char) {
        val currentG = g.create() as Graphics2D
        when (drawDirection) {
            'S' -> currentG.translate(positionX, positionY)
            'N' -> {
                currentG.translate(positionX + Images.imageSize.first, positionY + Images.imageSize.second)
                currentG.rotate(PI)
            }

            'E' -> {
                currentG.translate(positionX, positionY + Images.imageSize.second)
                currentG.rotate(3 * PI / 2)
            }

            'W' -> {
                currentG.translate(positionX + Images.imageSize.first, positionY)
                currentG.rotate(PI / 2)
            }
        }
        currentG.drawImage(image, 0, 0, null)

        currentG.dispose()
        movePosition(moveDirection)
    }

    drawImageAndMovePosition(Images.start, currentDirection, currentDirection)

    while (moves.isNotEmpty() && currentElementId < text.length) {
        val (newDirection, count) = moves.removeFirst()
        if (count > 1) {
            moves.add(0, ' ' to (count - 1))
        }

        val previousDirection = currentDirection

        currentDirection = when (newDirection) {
            ' ' -> previousDirection
            'L' -> when (previousDirection) {
                'N' -> 'W'
                'W' -> 'S'
                'S' -> 'E'
                'E' -> 'N'
                else -> error("Unknown current direction... $previousDirection")
            }

            'R' -> when (previousDirection) {
                'N' -> 'E'
                'E' -> 'S'
                'S' -> 'W'
                'W' -> 'N'
                else -> error("Unknown current direction... $previousDirection")
            }

            else -> error("Unknown new direction: $newDirection")
        }

        if (newDirection in setOf('L', 'R')) {
            val image = if (newDirection == 'L') Images.turnL else Images.turnR

            drawImageAndMovePosition(image, previousDirection, currentDirection)
        }

        drawImageAndMovePosition(
            Images.brainFuckCharToImage.getValue(text[currentElementId]),
            currentDirection,
            currentDirection
        )

        ++currentElementId
    }

    if (moves.isNotEmpty()) {
        println("Warning: not all moves are made (${moves.joinToString()})")
    }

    if (currentElementId < text.length) {
        println("Warning: not all chars are added, remaining: ${text.length - currentElementId}")
    }

    return result.cropToUsedSize()
}

fun generateAndSaveImage(text: String, rotations: String, loopsNeeded: Boolean): BufferedImage {
    val result = when {
        rotations.isNotEmpty() -> generateWithRotations(text, rotations)
        loopsNeeded -> generateColumnImageWithLoops(text)
        else -> generateColumnImage(text)
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
    val helloWorldRadiator =
        "N20-R3-R20-L3-L20-R2-R20-L1-L110"

    var text by remember { mutableStateOf(helloWorld) }
    var rotations by remember { mutableStateOf(helloWorldRadiator) }
    var lastImage by remember { mutableStateOf<BufferedImage?>(null) }
    var loopsNeeded by remember { mutableStateOf(false) }

    MaterialTheme {
        LazyColumn(Modifier.fillMaxWidth().padding(4.dp)) {
            item {
                TextField(
                    text,
                    onValueChange = { text = it },
                    Modifier.fillMaxWidth(),
                    placeholder = { Text("Please input text of a valid BrainFuck program") })
                Spacer(Modifier.height(2.dp))
                TextField(
                    rotations,
                    onValueChange = { rotations = it },
                    Modifier.fillMaxWidth(),
                    placeholder = { Text("Empty for a vertical curve") })
                Spacer(Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Button(onClick = {
                        lastImage = try {
                            generateAndSaveImage(text, rotations, loopsNeeded)
                        } catch (e: Exception) {
                            e.printStackTrace(); Images.error
                        }
                    }) {
                        Text("Curva!")
                    }
                    Spacer(Modifier.width(2.dp))
                    Checkbox(
                        if (rotations.isNotEmpty()) false else loopsNeeded,
                        onCheckedChange = { loopsNeeded = it },
                        enabled = rotations.isEmpty()
                    )
                    Spacer(Modifier.width(1.dp))
                    Text("Visualize loops" + " (unavailable with a path specified)".takeIf { rotations.isNotEmpty() }
                        .orEmpty(), color = if (rotations.isEmpty()) Color.Unspecified else MaterialTheme.colors.error)
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
    Window(title = "CurvaLang Generator", onCloseRequest = ::exitApplication) {
        App()
    }
}

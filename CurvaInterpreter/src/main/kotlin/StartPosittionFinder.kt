package lakotka.anton.curva

import java.awt.image.BufferedImage

data class Coordinates(
    val row: Int,
    val column: Int
) {
    override fun toString(): String = "(x=$column, y=$row)"
}

data class StartPosition(
    val coordinates: Coordinates,
    val orientation: Orientation
)

enum class Orientation {
    Normal,
    Left,
    UpsideDown,
    Right
}

fun BufferedImage.findStartPosition(): StartPosition {
    val coordinates = findShape(startShape) ?: kurwa("Can't find start position")

    return StartPosition(
        coordinates = Coordinates(
            row = coordinates.row + startShapeRowOffset,
            column = coordinates.column + startShapeColumnOffset
        ),
        orientation = Orientation.Normal
    )
}

private val startShape = """
*******
**###**
**###**
**###**
***#***
""".trim('\n').toBitMap()

private val startShapeRowOffset = 4
private val startShapeColumnOffset = 3

private fun BufferedImage.findShape(shapeBitMap: BitMap): Coordinates? {
    val tail = findTail(shapeBitMap.width, shapeBitMap.height) { tail ->
        tail.img.matchShape(shapeBitMap)
    }

    return tail?.topLeftCorner
}

private fun BufferedImage.matchShape(shapeBitMap: BitMap): Boolean {
    shapeBitMap.map.forEachIndexed { row, columns ->
        columns.forEachIndexed { column, blackPixelExpected ->
            val isBlack = getRGB(column, row) != -1
            if (blackPixelExpected != isBlack) return false
        }
    }

    return true
}

private class BitMap(
    val map: Array<Array<Boolean>>,
    val width: Int,
    val height: Int
) {
    fun value(at: Coordinates): Boolean = map[at.row][at.column]
}

private fun BufferedImage.findTail(width: Int, height: Int, matcher: (ImageTail) -> Boolean): ImageTail? {
    var row = 0

    while (row < this.height - height) {
        var column = 0
        while (column < this.width - width) {
            val tail = ImageTail(
                topLeftCorner = Coordinates(row, column),
                img = getSubimage(column, row, width, height)
            )
            if (matcher(tail)) return tail
            column += 1
        }
        row += 1
    }

    return null
}


private data class ImageTail(
    val topLeftCorner: Coordinates,
    val img: BufferedImage
)

private fun String.toBitMap(): BitMap {
    return lines().map { row ->
        row.map { ch ->
            when (ch) {
                '*' -> false
                '#' -> true
                else -> error("Invalid character '$ch'")
            }
        }.toTypedArray()
    }.toTypedArray().let{ BitMap(it, it[0].size, it.size) }
}

private fun BufferedImage.asciiArt(): String {
    return (0 until width).joinToString("") { column ->
        (0 until height).joinToString("\n") { row ->
            val pixel = getRGB(column, row)
            if (pixel == -1) "*" else "#"
        }
    }
}
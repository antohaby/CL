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
    Down,
    Right,
    Up,
    Left,
}

fun BufferedImage.findStartPosition(): StartPosition {
    for (startShape in startShapes) {
        val coordinates = findShape(startShape.shape) ?: continue
        return StartPosition(
            coordinates = Coordinates(
                row = coordinates.row + startShape.startShapeRowOffset,
                column = coordinates.column + startShape.startShapeColumnOffset
            ),
            orientation = startShape.orientation
        )
    }

    kurwa("Can't find start position")
}

private val startShapeBaseBitMap = """
*******
**###**
**###**
**###**
***#***
""".trim('\n').toBitMap()

private data class StartShape(
    val shape: BitMap = startShapeBaseBitMap,
    val orientation: Orientation = Orientation.Down,
    val startShapeRowOffset: Int = 4,
    val startShapeColumnOffset: Int = 3,
) {
    fun turn(): StartShape {
        val turned = shape.turn()
        val newOrientation = Orientation.values()[(orientation.ordinal + 1) % Orientation.values().size]

        val newRowOffset: Int
        val newColumnOffset: Int
        when (newOrientation) {
            Orientation.Down -> { newRowOffset = 4; newColumnOffset = 3 }
            Orientation.Right -> { newRowOffset = 3; newColumnOffset = 4 }
            Orientation.Up -> { newRowOffset = 0; newColumnOffset = 3 }
            Orientation.Left -> { newRowOffset = 3; newColumnOffset = 0 }
        }

        return StartShape(
            shape = turned,
            orientation = newOrientation,
            startShapeRowOffset = newRowOffset,
            startShapeColumnOffset = newColumnOffset,
        )
    }
}

private val startShapes: List<StartShape> = run {
    var currentShape = StartShape()
    val result = mutableListOf(currentShape)

    repeat(Orientation.values().size - 1) {
        currentShape = currentShape.turn()
        result.add(currentShape)
    }

    result
}

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

    fun center() = Coordinates(
        row = height / 2,
        column = width / 2
    )

    fun turn(around: Coordinates = center()): BitMap {
        val turned = Array(width) { newRow ->
            Array(height) { newCol ->
                val oldColumn = 2 * around.column - newRow
                val oldRow = newCol
                map[oldRow][oldColumn]
            }
        }

        return BitMap(
            map = turned,
            width = height,
            height = width
        )
    }
}

private fun BufferedImage.findTail(width: Int, height: Int, matcher: (ImageTail) -> Boolean): ImageTail? {
    var row = 0

    while (row <= this.height - height) {
        var column = 0
        while (column <= this.width - width) {
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

private fun BitMap.ascii(): String {
    return map.joinToString("\n") { rows ->
        rows.joinToString("") {
            if (it) "#" else "*"
        }
    }
}

private fun BufferedImage.asciiArt(): String {
    return (0 until width).joinToString("") { column ->
        (0 until height).joinToString("\n") { row ->
            val pixel = getRGB(column, row)
            if (pixel == -1) "*" else "#"
        }
    }
}
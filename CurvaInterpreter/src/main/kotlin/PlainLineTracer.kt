package lakotka.anton.curva

import java.awt.image.BufferedImage
import lakotka.anton.curva.Direction.*
import kotlin.math.abs

enum class Direction(val rowOffset: Int, val columnOffset: Int) {
    DOWN(1, 0),
    DOWN_RIGHT(1, 1),
    RIGHT(0, 1),
    UP_RIGHT(-1, 1),
    UP(-1, 0),
    UP_LEFT(-1, -1),
    LEFT(0, -1),
    DOWN_LEFT(1, -1);

    operator fun minus(that: Direction): Int {
        /**
         * Assume that Direction has counter clockwise order: DOWN -> DOWN_RIGHT -> RIGHT
         * Then if we substract ordinals we would got distance on how much changed the direction
         * And then we simply map it into TurnAndIntersection
         */
        var diff = that.ordinal - this.ordinal
        if (diff > 4) diff -= 8
        if (diff < -4) diff += 8

        return diff
    }
}

fun BufferedImage.traceToDirections(): List<Direction> {
    val startPosition = findStartPosition()
    return traceFrom(startPosition)
}

private fun BufferedImage.traceFrom(startPosition: StartPosition): List<Direction> {
    val result = mutableListOf<Direction>()

    var row = startPosition.coordinates.row
    var column = startPosition.coordinates.column
    var direction = DOWN // startPosition.orientation

    while (row >= 0 && row < height && column >= 0 && column < width) {
        val peek = peek(direction, row, column)

        val stickToOldDirection = peek[direction]!!

        if (!stickToOldDirection) {
            val possibleDirections = peek.filterValues { it }.keys
            if (possibleDirections.isEmpty()) return result

            val newDirection = possibleDirections.minBy { abs(it - direction) }
            direction = newDirection
        }

        result.add(direction)
        row += direction.rowOffset
        column += direction.columnOffset
    }

    error("Out of bounds ($column, $row)")
}

private fun BufferedImage.peek(direction: Direction, row: Int, column: Int): Map<Direction, Boolean> =
    allowedToPeek[direction]!!
        .associateWith { pixel(row + it.rowOffset, column + it.columnOffset) }

private fun directionValueAt(idx: Int): Direction {
    val size = Direction.values().size
    if (idx < 0)
        return Direction.values()[size + idx]
    return Direction.values()[idx % size]
}

private val allowedToPeek: Map<Direction, List<Direction>> = run {
    Direction.values().associateWith { direction ->
        (-2..2).map { directionValueAt(direction.ordinal + it) }
    }
}

private fun BufferedImage.pixel(row: Int, column: Int): Boolean {
    if (row >= height) return false
    if (column >= width) return false
    return getRGB(column, row) != -1
}

// X -> Column
// Y -> Row
fun main() {

}
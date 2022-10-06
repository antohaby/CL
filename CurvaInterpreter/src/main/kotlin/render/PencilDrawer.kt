package lakotka.anton.curva.render

import lakotka.anton.curva.Coordinates
import lakotka.anton.curva.Direction
import lakotka.anton.curva.TurnAndIntersection
import lakotka.anton.curva.kurwaInternal
import java.awt.Color
import java.awt.image.BufferedImage

class PencilDrawer(
    private val img: BufferedImage,
    startCoordinates: Coordinates,
    startDirection: Direction
) {
    private var direction = startDirection

    private var currentRow = startCoordinates.row
    set(value) {
        if (value < 0 || value >= img.height)
            kurwaInternal("Out of bounds: $currentCol x $value vs ${img.width} x ${img.height}")
        field = value
    }

    private var currentCol = startCoordinates.column
    set(value) {
        if (value < 0 || value >= img.width)
            kurwaInternal("Out of bounds: $value x $currentRow vs ${img.width} x ${img.height}")
        field = value
    }

    val currentCoordinates: Coordinates get() = Coordinates(currentRow, currentCol)

    val currentDirection get() = direction

    private fun pixel(row: Int, col: Int) = img.setRGB(col, row, Color.BLACK.rgb)

    fun drawStartPoint() {
        pixel(currentRow, currentCol)

        val or: Int
        val oc: Int
        when (direction) {
            Direction.DOWN -> { oc = 1; or = 3; }
            Direction.RIGHT -> { oc = 3; or = 1; }
            Direction.UP -> { oc = 1; or = -1; }
            Direction.LEFT -> { oc = -1; or = 1; }
            Direction.UP_RIGHT,
            Direction.UP_LEFT,
            Direction.DOWN_RIGHT,
            Direction.DOWN_LEFT -> kurwaInternal("Unsupported")
        }

        val r = currentRow - or
        val c = currentCol - oc

        repeat(3) { row ->
            repeat(3) { col ->
                pixel(r+row, c+col)
            }
        }
    }

    fun drawLine(size: Int) {
        repeat(size) {
            currentCol += direction.columnOffset
            currentRow += direction.rowOffset

            img.setRGB(currentCol, currentRow, Color.BLACK.rgb)
        }
    }

    fun turn(turn: TurnAndIntersection) {
        direction += turn
    }

    fun move(row: Int, col: Int) {
        currentRow = row
        currentCol = col
    }
}
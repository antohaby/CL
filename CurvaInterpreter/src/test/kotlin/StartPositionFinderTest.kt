package lakotka.anton.curva

import java.io.File
import javax.imageio.ImageIO
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class StartPositionFinderTest {
    @Test
    fun testDown() = assertFile("down_26x66")

    @Test
    fun testRightSimple() = assertFile("right_5x3")

    @Test
    fun testRight() = assertFile("right_28x64")

    @Test
    fun testUpSimple() = assertFile("up_3x3")

    @Test
    fun testUp() = assertFile("up_36x63")

    @Test
    fun testLeft() = assertFile("left_46x74")

    @Test
    fun testUpNoisy() = assertFile("up_26x63_noisy")

    fun assertFile(name: String) {
        val file = this.javaClass
            .getResource("/start_position/$name.png")
            .toURI()
            .let(::File)

        val nameParts = file
            .name
            .removeSuffix(".png")
            .split("_")

        val orientation = when(nameParts[0]) {
            "up" -> Orientation.Up
            "down" -> Orientation.Down
            "left" -> Orientation.Left
            "right" -> Orientation.Right
            else -> error("Invalid name: $file")
        }

        val (column, row) = nameParts[1].split("x").map { it.toInt() }

        val startPosition = try {
            ImageIO.read(file).findStartPosition()
        } catch (e: Throwable) { fail("Cant assert $file: $e") }
        val expected = StartPosition(
            coordinates = Coordinates(row, column),
            orientation = orientation
        )

        assertEquals(expected, startPosition)
    }
}
package lakotka.anton.curva.render

import lakotka.anton.curva.*
import lakotka.anton.curva.CurvaToken.TurnLeft
import lakotka.anton.curva.CurvaToken.TurnRight
import lakotka.anton.curva.TurnAndIntersection.*
import java.awt.Color
import java.awt.image.BufferedImage
import java.awt.image.BufferedImage.TYPE_BYTE_BINARY
import java.io.File
import javax.imageio.ImageIO
import kotlin.math.sqrt

fun renderCurvaFromBrainFuck(bfCode: String, toFile: File) {
    val bfTokens = bfCode.tokenizeBrainFuck()
    val clTokens = bfTokens.toCurvaLangTokens()
    val turns = clTokens.flattenToTurns()

    val estimateSize = 101
    val img = whiteCanvas(estimateSize, estimateSize)

    img.drawSnake(clTokens)

    ImageIO.write(img, "png", toFile)
}

private fun whiteCanvas(width: Int, height: Int): BufferedImage {
    val img = BufferedImage(width, height, TYPE_BYTE_BINARY)
    val graphics = img.createGraphics()
    graphics.background = Color.WHITE
    graphics.clearRect(0, 0, img.width, img.height)

    return img
}

private fun List<BrainFuckToken>.toCurvaLangTokens(): List<CurvaToken> = map { token -> token.toCurvaLangToken }
private fun List<CurvaToken>.flattenToTurns(): List<TurnAndIntersection> = flatMap { it.turns }

private fun BufferedImage.drawSnake(tokens: List<CurvaToken>, snakeWidth: Int = 6) {
    val startPosition = Coordinates(column = snakeWidth / 2, row = 4)
    val pencil = PencilDrawer(this, startPosition, Direction.DOWN)
    val tokenDrawer = TokenShapeDrawer(pencil)
    pencil.drawStartPoint()

    for (token in tokens) {
        val currentCoordinates = pencil.currentCoordinates

        when (pencil.currentDirection) {
            Direction.DOWN -> if (currentCoordinates.row + 2*snakeWidth >= height) tokenDrawer.drawLeftUTurn()
            Direction.UP -> if (currentCoordinates.row - 2*snakeWidth < 0) tokenDrawer.drawRightUTurn()
            else -> kurwaInternal("WTF!")
        }

        tokenDrawer.drawToken(token)
    }
}


// Token Shapes
class TokenShapeDrawer(
    private val drawer: PencilDrawer,
    private val distanceBetweenBlocks: Int = 1,
    private val lineLength: Int = 2
) {
    fun drawAll(tokens: List<CurvaToken>) {
        drawer.drawStartPoint()
        for (token in tokens) {
            drawToken(token)
        }
    }

    fun drawToken(curvaToken: CurvaToken) {
        with (curvaToken) {
            when (this) {
                CurvaToken.Inc -> drawTurns()
                CurvaToken.Dec -> drawTurns()
                CurvaToken.WhileBegin -> drawWhileBegin()
                CurvaToken.WhileEnd -> drawWhileEnd()
                CurvaToken.IncPointer -> drawTurns()
                CurvaToken.DecPointer -> drawTurns()
                CurvaToken.Write -> drawWrite()
                CurvaToken.Read -> drawRead()
                TurnLeft -> drawTurns()
                TurnRight -> drawTurns()
            }
        }
    }

    fun drawLeftUTurn() {
        TurnLeft.drawTurns()
        TurnLeft.drawTurns()
    }

    fun drawRightUTurn() {
        TurnRight.drawTurns()
        TurnRight.drawTurns()
    }

    private fun CurvaToken.drawTurns() {
        for (turn in turns) {
            space()
            turn(turn)
            line()
        }
    }

    private fun space() = drawer.drawLine(distanceBetweenBlocks)
    private fun line() = drawer.drawLine(lineLength)
    private fun turn(turn: TurnAndIntersection) = drawer.turn(turn)

    private fun drawWhileBegin() {
        space()
        turn(SmoothRight); line()
        turn(TangentLeft); line(); line()
        turn(TangentRight); line()
        turn(SmoothLeft); line()
    }

    private fun drawWhileEnd() {
        space()
        turn(SmoothLeft); line()
        turn(TangentRight); line(); line()
        turn(TangentLeft); line()
        turn(SmoothRight); line()
    }

    private fun drawWrite() {
        space()
        turn(TangentLeft); line()
        turn(TangentRight); space()
        turn(SmoothRight); line(); line()
        turn(SmoothLeft); space();
        turn(TangentLeft); line()
        turn(TangentRight); line()
    }

    private fun drawRead() {
        // TangentRight, TangentLeft, SmoothLeft, SmoothRight, TangentRight, TangentLeft
        space()
        turn(TangentRight); line()
        turn(TangentLeft); space()
        turn(SmoothLeft); line(); line()
        turn(SmoothRight); space();
        turn(TangentRight); line()
        turn(TangentLeft); line()
    }
}

private val helloWorld = "++++++++[>++++[>++>+++>+++>+<<<<-]>+>+>->>+[<]<-]>>.>---.+++++++..+++.>>.<-.<.+++.------.--------.>>+.>++."

fun main() {
    renderCurvaFromBrainFuck(helloWorld, File("out.png"))
    // val img = whiteCanvas(500, 500)
    // val pencil = PencilDrawer(img, Coordinates(4, 4), Direction.DOWN)
    // val tokenDrawer = TokenShapeDrawer(pencil)
    //
    // pencil.drawStartPoint()
    // for (token in CurvaToken.values()) {
    //     tokenDrawer.drawToken(token)
    // }
    //
    // val out = File("out.png")
    // ImageIO.write(img, "png", out)
}
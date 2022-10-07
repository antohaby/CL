package lakotka.anton.curva.render

import lakotka.anton.curva.*
import lakotka.anton.curva.CurvaToken.TurnLeft
import lakotka.anton.curva.CurvaToken.TurnRight
import lakotka.anton.curva.TurnAndIntersection.*
import java.awt.Color
import java.awt.image.BufferedImage
import java.awt.image.BufferedImage.TYPE_BYTE_BINARY
import java.io.File
import java.util.*
import javax.imageio.ImageIO
import kotlin.collections.ArrayDeque

fun renderCurvaFromBrainFuckAsSnake(bfCode: String, width: Int, height: Int, toFile: File) {
    val bfTokens = bfCode.tokenizeBrainFuck()
    val clTokens = bfTokens.toCurvaLangTokens()

    val img = whiteCanvas(width, height)
    val startPosition = Coordinates(column = 3, row = 4)
    val pencil = PencilDrawer(img, startPosition, Direction.DOWN)
    val tokenDrawer = TokenShapeDrawer(pencil)
    pencil.drawStartPoint()

    for (token in clTokens) {
        val currentCoordinates = pencil.currentCoordinates

        when (pencil.currentDirection) {
            Direction.DOWN -> if (currentCoordinates.row + 15 >= height) tokenDrawer.drawLeftUTurn()
            Direction.UP -> if (currentCoordinates.row - 15 < 0) tokenDrawer.drawRightUTurn()
            else -> kurwaInternal("WTF!")
        }

        tokenDrawer.drawToken(token)
    }

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

data class TurnStrategyContext(
    val img: BufferedImage,
    val pencil: PencilDrawer,
    val tokenDrawer: TokenShapeDrawer,
    val token: CurvaToken
)
typealias TurnStrategy = (context: TurnStrategyContext) -> List<TurnAndIntersection?>

data class DrawnTokens(
    val token: CurvaToken,
    var turn: CurvaToken?,
    var availableTurns: MutableList<TurnAndIntersection?>?
)

fun renderCurvaFromBrainFuckAsOutlineStroke(
    bfCode: String,
    sizeWidth: Int,
    sizeHeight: Int,
    startPosition: Coordinates,
    toFile: File,
    turnStrategy: TurnStrategy
) {
    val bfTokens = bfCode.tokenizeBrainFuck()
    val clTokens = bfTokens.toCurvaLangTokens()

    //val img = ImageIO.read(inputImage)
    val img = whiteCanvas(sizeWidth, sizeHeight)
    val pencil = PencilDrawer(img, startPosition, Direction.RIGHT)
    val tokenDrawer = TokenShapeDrawer(pencil)
    pencil.drawStartPoint()

    val tokensToDraw = clTokens.map { DrawnTokens(it,  null,null) }.let { LinkedList(it) }
    val tokensDrawn = Stack<DrawnTokens>()

    fun drawBack() {
        while (true) {
            if (tokensDrawn.isEmpty()) kurwaInternal("Can't draw back")

            val drawnToken = tokensDrawn.pop()
            tokenDrawer.drawBackToken(drawnToken.token)
            if (drawnToken.turn != null) tokenDrawer.drawBackToken(drawnToken.turn!!)

            tokensToDraw.addFirst(drawnToken)

            val availableTurns = drawnToken.availableTurns
            if (availableTurns != null && availableTurns.isNotEmpty()) {
                break
            } else {
                drawnToken.availableTurns = null
            }
        }
    }

    //var step = 0
    while (tokensToDraw.isNotEmpty()) {
        // ImageIO.write(img, "png", File("tmp/$step.png"))
        // step++
        val entry = tokensToDraw.removeFirst()
        val token = entry.token

        val availableTurns: MutableList<TurnAndIntersection?>
        if (entry.availableTurns != null) {
            availableTurns = entry.availableTurns!!
        } else {
            availableTurns = turnStrategy(TurnStrategyContext(img, pencil, tokenDrawer, token)).toMutableList()
        }

        if (availableTurns.isEmpty()) {
            drawBack()
            continue
        }

        val turn = availableTurns.removeFirst()
        val turnToken = when (turn) {
            TangentLeft -> TurnLeft
            TangentRight -> TurnRight
            null -> null
            else -> kurwaInternal("WTF")
        }

        if (turnToken != null) tokenDrawer.drawToken(turnToken)
        tokenDrawer.drawToken(token)

        entry.turn = turnToken
        entry.availableTurns = availableTurns
        tokensDrawn.push(entry)
    }

    ImageIO.write(img, "png", toFile)
}

fun striveToPoint(striveToPoint: Coordinates): TurnStrategy = l@{ (img, pencil, tokenDrawer, _) ->
    val currentCoordinates = pencil.currentCoordinates
    fun isObstacleAhead(direction: Direction) = img.isObstacleAhead(currentCoordinates, direction)

    fun distance(direction: Direction): Long {
        val newRow = (currentCoordinates.row + direction.rowOffset*5).toLong()
        val newCol = (currentCoordinates.column + direction.columnOffset*5).toLong()
        return (newRow - striveToPoint.row) * (newRow - striveToPoint.row) + (newCol - striveToPoint.column) * (newCol - striveToPoint.column)
    }

    val direction = pencil.currentDirection
    val availableTurns = listOf(
        TangentLeft,
        null,
        TangentRight
    ).sortedBy { distance(direction + it) }
        .filterNot { isObstacleAhead(direction + it) }

    availableTurns
}

fun spiral(context: TurnStrategyContext): List<TurnAndIntersection?> {
    val (img, pencil, tokenDrawer, _) = context

    val currentCoordinates = pencil.currentCoordinates
    fun isObstacleAhead(direction: Direction) = img.isObstacleAhead(currentCoordinates, direction)

    val direction = pencil.currentDirection

    return listOf(TangentLeft, null, TangentRight)
        .filterNot { isObstacleAhead(direction + it) }
}

private fun BufferedImage.isObstacleAhead(
    coordinates: Coordinates,
    direction: Direction,
    sizeHeight: Int = 15,
    sizeWidth: Int = 5,
    gap: Int = 5
): Boolean {
    val r: Int
    val c: Int
    val w: Int
    val h: Int
    when (direction) {
        Direction.DOWN -> { r = coordinates.row + gap; c = coordinates.column - sizeWidth / 2; w = sizeWidth; h = sizeHeight }
        Direction.RIGHT -> { r = coordinates.row - sizeWidth / 2; c = coordinates.column + gap; w = sizeHeight; h = sizeWidth }
        Direction.UP -> { r = coordinates.row - sizeHeight - gap; c = coordinates.column - sizeWidth / 2; w = sizeWidth; h = sizeHeight }
        Direction.LEFT -> { r = coordinates.row - sizeWidth / 2; c = coordinates.column - sizeHeight - gap; w = sizeHeight; h = sizeWidth  }
        else -> kurwaInternal("Unsupported")
    }

    if (r < 0 || r >= height || r + h >= height) return true
    if (c < 0 || c >= width || c + w >= width) return true

    (r until r + h).forEach { row ->
        (c until c + w).forEach l@{ col ->
            if (row == coordinates.row && col == coordinates.column) return@l
            val pixel = getRGB(col, row)
            if (pixel != Color.WHITE.rgb) return true
        }
    }

    return false
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

    private fun CurvaToken.drawBackTurns() {
        for (turn in turns.reversed()) {
            eraseLine()
            unTurn(turn)
            eraseSpace()
        }
    }

    private fun space() = drawer.drawLine(distanceBetweenBlocks)
    private fun line() = drawer.drawLine(lineLength)
    private fun turn(turn: TurnAndIntersection) = drawer.turn(turn)

    private fun eraseLine() = drawer.eraseLine(lineLength)
    private fun eraseSpace() = drawer.eraseLine(distanceBetweenBlocks)
    private fun unTurn(turn: TurnAndIntersection) = drawer.turnBack(turn)

    fun drawBackToken(token: CurvaToken) {
        with(token) {
            when (this) {
                CurvaToken.Inc -> drawBackTurns()
                CurvaToken.Dec -> drawBackTurns()
                CurvaToken.WhileBegin -> drawBackWhileBegin()
                CurvaToken.WhileEnd -> drawBackWhileEnd()
                CurvaToken.IncPointer -> drawBackTurns()
                CurvaToken.DecPointer -> drawBackTurns()
                CurvaToken.Write -> drawBackWrite()
                CurvaToken.Read -> drawBackRead()
                TurnLeft -> drawBackTurns()
                TurnRight -> drawBackTurns()
            }
        }
    }

    private fun drawWhileBegin() {
        space()
        turn(SmoothRight); line()
        turn(TangentLeft); line(); line()
        turn(TangentRight); line()
        turn(SmoothLeft); line()
    }

    private fun drawBackWhileBegin() {
        eraseLine(); unTurn(SmoothLeft)
        eraseLine(); unTurn(TangentRight)
        eraseLine(); eraseLine(); unTurn(TangentLeft)
        eraseLine(); unTurn(SmoothRight)
        eraseSpace()
    }

    private fun drawWhileEnd() {
        space()
        turn(SmoothLeft); line()
        turn(TangentRight); line(); line()
        turn(TangentLeft); line()
        turn(SmoothRight); line()
    }

    private fun drawBackWhileEnd() {
        eraseLine(); unTurn(SmoothRight)
        eraseLine(); unTurn(TangentLeft)
        eraseLine(); eraseLine(); unTurn(TangentRight)
        eraseLine(); unTurn(SmoothLeft)
        eraseSpace()
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

    private fun drawBackWrite() {
        eraseLine(); unTurn(TangentRight)
        eraseLine(); unTurn(TangentLeft)
        eraseSpace(); unTurn(SmoothLeft)
        eraseLine(); eraseLine(); unTurn(SmoothRight)
        eraseSpace(); unTurn(TangentRight)
        eraseLine(); unTurn(TangentLeft)
        eraseSpace()
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

    private fun drawBackRead() {
        eraseLine(); unTurn(TangentLeft)
        eraseLine(); unTurn(TangentRight)
        eraseSpace(); unTurn(SmoothRight)
        eraseLine(); eraseLine(); unTurn(SmoothLeft)
        eraseSpace(); unTurn(TangentLeft)
        eraseLine(); unTurn(TangentRight)
        eraseSpace()
    }
}

private val helloWorld = "++++++++[>++++[>++>+++>+++>+<<<<-]>+>+>->>+[<]<-]>>.>---.+++++++..+++.>>.<-.<.+++.------.--------.>>+.>++."

fun main() {
    val mandelbrot = File("mandelbrot.b").readText()
    val mandelbrotBase = File("mandelbrot_boundary2.png")
    val out = File("out.png")

    renderCurvaFromBrainFuckAsSnake(mandelbrot, 1550, 486, File("out.png"))
    // renderCurvaFromBrainFuckAsOutlineStroke(
    //     mandelbrot,
    //     startPosition = Coordinates(column = 200, row = 200),
    //     sizeHeight = 500,
    //     sizeWidth = 500,
    //     toFile = out,
    //     turnStrategy = ::spiral
    // )
}
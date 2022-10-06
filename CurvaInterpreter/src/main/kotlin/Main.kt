package lakotka.anton.curva

import java.io.File
import javax.imageio.ImageIO

private val helloWorld = "++++++++[>++++[>++>+++>+++>+<<<<-]>+>+>->>+[<]<-]>>.>---.+++++++..+++.>>.<-.<.+++.------.--------.>>+.>++."

fun main(args: Array<String>) {
    val mode = args.getOrNull(0) ?: "bf"

    when (mode) {
        "bf" -> {
            val file = args.getOrNull(1)
            runBrainFuck(file)
        }
        "cl" -> {
            val file = args.getOrNull(1) ?: kurwa("file must be passed as second argument")
            runCurvaLang(file)
        }
        else -> kurwa("Unknown execution mode '$mode'")
    }
}

private fun runBrainFuck(filePath: String?) {
    val code = filePath?.let(::File)?.readText() ?: helloWorld
    val tokens = code.tokenizeBrainFuck()
    val program = tokens.parseBrainFuck()

    program.runProgram()
}

private fun runCurvaLang(filePath: String) {
    val fileImg = File(filePath)
    val program = curvaLangProgram(fileImg)

    kurwaInfo("Executing code: ${program.toBrainFuckCode()}")
    program.runProgram()
}

fun curvaLangProgram(fileImg: File): List<Command> {
    val img = ImageIO.read(fileImg)
    val startPosition = img.findStartPosition()
    val directions = img.traceToDirections(startPosition)
    val turns = directions.traceToTurns()
    val tokens = turns.tokenizeToBrainFuck()
    return tokens.parseBrainFuck()
}

private fun List<Command>.runProgram() {
    val runner = Runner(
        program = this,
        memory = ByteArray(32*1024) { 0 },
        io = StdIO
    )
    runner.run()
}
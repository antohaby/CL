package lakotka.anton.curva

private val helloWorld = "++++++++[>++++[>++>+++>+++>+<<<<-]>+>+>->>+[<]<-]>>.>---.+++++++..+++.>>.<-.<.+++.------.--------.>>+.>++."

fun main(args: Array<String>) {
    val code = args.firstOrNull() ?: helloWorld
    runBrainFuck(code)
}

private fun runBrainFuck(code: String) {
    val program = parseBrainFuckString(code)
    val runner = Runner(
        program = program,
        memory = ByteArray(32*1024) { 0 },
        io = StdIO
    )

    runner.run()
}
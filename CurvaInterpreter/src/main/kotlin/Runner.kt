package lakotka.anton.curva

import lakotka.anton.curva.Command.*

private const val zero: Byte = 0

class Runner(
    val program: List<Command>,
    val memory: ByteArray,
    val io: IO
) {
    private var commandPointer = 0

    private var pointer: Int = 0
        set(value) {
            field = if (value < 0) {
                memory.size - value
            } else {
                value % memory.size
            }
        }

    interface IO {
        fun read(): Byte
        fun write(byte: Byte)
    }

    fun run() {
        while(commandPointer < program.size) {
            val command = program[commandPointer]
            commandPointer++

            command.execute()
        }
    }

    private fun Command.execute() {
        when(this) {
            Increment -> memory[pointer]++
            Decrement -> memory[pointer]--
            IncrementPointer -> pointer++
            DecrementPointer -> pointer--
            is WhileBegin -> if (memory[pointer] == zero) commandPointer = end.positionValue
            is WhileEnd -> if (memory[pointer] != zero) commandPointer = begin.positionValue
            Write -> io.write(memory[pointer])
            Read -> memory[pointer] = io.read()
        }
    }
}

object StdIO : Runner.IO {
    override fun read(): Byte {
        //return readln().first().code.toByte()
        return System.`in`.read().toByte()
    }

    override fun write(byte: Byte) {
        print(byte.toInt().toChar())
    }
}
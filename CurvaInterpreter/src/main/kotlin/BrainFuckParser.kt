package lakotka.anton.curva

import lakotka.anton.curva.Command.*
import java.util.*

fun List<BrainFuckToken>.parseBrainFuck(): List<Command> {
    val commands = mutableListOf<Command?>()
    val whileBlockTracker = WhileBlockTracker()

    for ((position, token) in this.withIndex()) {
        val command = when(token) {
            BrainFuckToken.IncPointer -> Command.IncrementPointer
            BrainFuckToken.DecPointer -> Command.DecrementPointer
            BrainFuckToken.Increment -> Command.Increment
            BrainFuckToken.Decrement -> Command.Decrement
            BrainFuckToken.WhileBegin -> { whileBlockTracker.rememberWhileBegin(Position(position)); null }
            BrainFuckToken.WhileEnd -> { whileBlockTracker.rememberWhileEnd(Position(position)); null }
            BrainFuckToken.Read -> Command.Read
            BrainFuckToken.Write -> Command.Write
        }

        commands.add(command)
    }

    val whileBlocks = whileBlockTracker.whileBlocks.toMutableList()
    // Fill in "while blocks"
    for (block in whileBlocks) {
        if (commands[block.begin.positionValue] !== null) kurwaInternal("Unexpected while begin block")
        if (commands[block.end.positionValue] !== null) kurwaInternal("Unexpected while begin block")

        commands[block.begin.positionValue] = Command.WhileBegin(block.end)
        commands[block.end.positionValue] = Command.WhileEnd(block.begin)
    }

    @Suppress("UNCHECKED_CAST")
    return commands as List<Command>
}

class WhileBlockTracker {
    class WhileBlock(
        var begin: Position,
        var end: Position
    )

    private val _whileMarkers = mutableListOf<WhileBlock>()
    private val stack = Stack<Position>()

    fun rememberWhileBegin(startPosition: Position) {
        stack.push(startPosition)
    }

    fun rememberWhileEnd(endPosition: Position) {
        if (stack.isEmpty()) kurwa("Unexpected [WhileEnd] at ${endPosition.positionValue}")
        val startPosition = stack.pop()
        _whileMarkers.add(WhileBlock(startPosition, endPosition))
    }

    val whileBlocks get(): List<WhileBlock> {
        if (stack.isNotEmpty()) kurwa("Expected [WhileEnd]")

        return _whileMarkers.toList()
    }
}

fun List<Command>.toBrainFuckCode() = joinToString("") { command ->
    when(command) {
        Increment -> "+"
        Decrement -> "-"
        DecrementPointer -> "<"
        IncrementPointer -> ">"
        Read -> ","
        is WhileBegin -> "["
        is WhileEnd -> "]"
        Write -> "."
    }
}
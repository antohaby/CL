package lakotka.anton.curva

import lakotka.anton.curva.Command.*
import java.util.*

/**
    > = increases memory pointer, or moves the pointer to the right 1 block.
    < = decreases memory pointer, or moves the pointer to the left 1 block.
    + = increases value stored at the block pointed to by the memory pointer
    - = decreases value stored at the block pointed to by the memory pointer
    [ = like c while(cur_block_value != 0) loop.
    ] = if block currently pointed to's value is not zero, jump back to [
    , = like c getchar(). input 1 character.
    . = like c putchar(). print 1 character to the console
 */
fun parseBrainFuckString(code: String): List<Command> {
    val commands = mutableListOf<Command?>()
    val whileBlockTracker = WhileBlockTracker()

    for ((position, token) in code.withIndex()) {
        val command = when(token) {
            '>' -> IncrementPointer
            '<' -> DecrementPointer
            '+' -> Increment
            '-' -> Decrement
            '[' -> { whileBlockTracker.rememberWhileBegin(Position(position)); null }
            ']' -> { whileBlockTracker.rememberWhileEnd(Position(position)); null }
            ',' -> Read
            '.' -> Write
            else -> kurwa("Unexpected token: '$token'")
        }

        commands.add(command)
    }

    val whileBlocks = whileBlockTracker.whileBlocks.toMutableList()
    // Fill in "while blocks"
    for (block in whileBlocks) {
        if (commands[block.begin.positionValue] !== null) kurwaInternal("Unexpected while begin block")
        if (commands[block.end.positionValue] !== null) kurwaInternal("Unexpected while begin block")

        commands[block.begin.positionValue] = WhileBegin(block.end)
        commands[block.end.positionValue] = WhileEnd(block.begin)
    }

    @Suppress("UNCHECKED_CAST")
    return commands as List<Command>
}

private class WhileBlockTracker {
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

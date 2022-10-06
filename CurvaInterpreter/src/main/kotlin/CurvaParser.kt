package lakotka.anton.curva

import java.io.File
import lakotka.anton.curva.TurnAndIntersection.*

// Tokens
private val inc = listOf(SmoothLeft, TangentRight, SmoothLeft)
private val dec = listOf(SmoothRight, TangentLeft, SmoothRight)
private val whileBegin = listOf(SmoothRight, TangentLeft, TangentRight, SmoothLeft)
private val whileEnd = listOf(SmoothLeft, TangentRight, TangentLeft, SmoothRight)
private val incPointer = listOf(TangentLeft, TangentRight, TangentRight, TangentLeft)
private val decPointer = listOf(TangentRight, TangentLeft, TangentLeft, TangentRight)
private val write = listOf(TangentLeft, TangentRight, SmoothRight, SmoothLeft, TangentLeft, TangentRight)
private val read = listOf(TangentRight, TangentLeft, SmoothLeft, SmoothRight, TangentRight, TangentLeft)

private val tokenMap = mapOf(
    BrainFuckToken.Increment to inc,
    BrainFuckToken.Decrement to dec,
    BrainFuckToken.WhileBegin to whileBegin,
    BrainFuckToken.WhileEnd to whileEnd,
    BrainFuckToken.IncPointer to incPointer,
    BrainFuckToken.DecPointer to decPointer,
    BrainFuckToken.Write to write,
    BrainFuckToken.Read to read,
)

private val minBuffSize: Int = tokenMap.values.minOf { it.size }
private val maxBuffSize: Int = tokenMap.values.maxOf { it.size }

fun List<TurnAndIntersection>.tokenizeToBrainFuck(): List<BrainFuckToken> {
    val buff = mutableListOf<TurnAndIntersection>()
    val result = mutableListOf<BrainFuckToken>()
    for (turn in this) {
        buff.add(turn)

        if (buff.size >= minBuffSize) {
            val tokenFound = tokenMap.entries.firstOrNull { it.value == buff }
            if (tokenFound != null) {
                buff.clear()
                result.add(tokenFound.key)
            }
        }

        if (buff.size > maxBuffSize) kurwa("Unknown shape: $buff")
    }

    return result
}

enum class TurnAndIntersection {
    TangentLeft,
    TangentRight,
    SharpLeft,
    SharpRight,
    SmoothLeft,
    SmoothRight,
}
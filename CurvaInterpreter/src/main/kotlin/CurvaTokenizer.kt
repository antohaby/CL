package lakotka.anton.curva

import lakotka.anton.curva.TurnAndIntersection.*

// Tokens
enum class TurnAndIntersection {
    TangentLeft,
    TangentRight,
    SharpLeft,
    SharpRight,
    SmoothLeft,
    SmoothRight,
}

enum class CurvaToken(val turns: List<TurnAndIntersection>) {
    Inc(listOf(SmoothLeft, TangentRight, SmoothLeft)),
    Dec(listOf(SmoothRight, TangentLeft, SmoothRight)),
    WhileBegin(listOf(SmoothRight, TangentLeft, TangentRight, SmoothLeft)),
    WhileEnd(listOf(SmoothLeft, TangentRight, TangentLeft, SmoothRight)),
    IncPointer(listOf(TangentLeft, TangentRight, TangentRight, TangentLeft)),
    DecPointer(listOf(TangentRight, TangentLeft, TangentLeft, TangentRight)),
    Write(listOf(TangentLeft, TangentRight, SmoothRight, SmoothLeft, TangentLeft, TangentRight)),
    Read(listOf(TangentRight, TangentLeft, SmoothLeft, SmoothRight, TangentRight, TangentLeft)),
    TurnLeft(listOf(SmoothLeft, SmoothLeft)),
    TurnRight(listOf(SmoothRight, SmoothRight)),
}

private val minBuffSize: Int = CurvaToken.values().minOf { it.turns.size }
private val maxBuffSize: Int = CurvaToken.values().maxOf { it.turns.size }

private val tokenMap = CurvaToken.values().associateBy { it.turns }
private val tokenIgnore = setOf(CurvaToken.TurnLeft, CurvaToken.TurnRight)

fun List<TurnAndIntersection>.tokenizeToBrainFuck(): List<BrainFuckToken> {
    val buff = mutableListOf<TurnAndIntersection>()
    val result = mutableListOf<BrainFuckToken>()
    for (turn in this) {
        buff.add(turn)
        if (buff.size >= minBuffSize) {
            val tokenFound = tokenMap[buff]
            if (tokenFound != null) {
                buff.clear()
                val brainFuckToken = tokenFound.brainFuckToken
                if (brainFuckToken != null) result.add(brainFuckToken)
            }
        }

        if (buff.size > maxBuffSize) kurwa("Unknown shape: $buff")
    }

    return result
}


private val CurvaToken.brainFuckToken: BrainFuckToken? get() =
    when(this) {
        CurvaToken.Inc -> BrainFuckToken.Increment
        CurvaToken.Dec -> BrainFuckToken.Decrement
        CurvaToken.WhileBegin -> BrainFuckToken.WhileBegin
        CurvaToken.WhileEnd -> BrainFuckToken.WhileEnd
        CurvaToken.IncPointer -> BrainFuckToken.IncPointer
        CurvaToken.DecPointer -> BrainFuckToken.DecPointer
        CurvaToken.Write -> BrainFuckToken.Write
        CurvaToken.Read -> BrainFuckToken.Read
        CurvaToken.TurnLeft -> null
        CurvaToken.TurnRight -> null
    }
package lakotka.anton.curva

internal fun List<Direction>.traceToTurns(): List<TurnAndIntersection> {
    return zipWithNext { a, b -> a.turnTo(b) }.filterNotNull()
}

private fun Direction.turnTo(that: Direction): TurnAndIntersection? {
    return when (this - that) {
        0 -> null // no change
        1 -> TurnAndIntersection.SmoothLeft
        2 -> TurnAndIntersection.TangentLeft
        3 -> TurnAndIntersection.SharpLeft
        -1 -> TurnAndIntersection.SmoothRight
        -2 -> TurnAndIntersection.TangentRight
        -3 -> TurnAndIntersection.SharpRight
        else -> kurwaInternal("Unsupported turn difference (from $this to $that)")
    }
}
package lakotka.anton.curva

@JvmInline
value class Position(val positionValue: Int)

sealed class Command  {
    object Increment : Command()
    object Decrement : Command()
    object IncrementPointer : Command()
    object DecrementPointer : Command()
    class WhileBegin(val end: Position) : Command()
    class WhileEnd(val begin: Position) : Command()
    object Write : Command()
    object Read : Command()
}
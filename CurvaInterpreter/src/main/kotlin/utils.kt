package lakotka.anton.curva

fun kurwa(msg: String): Nothing {
    error("[Curva Error]: $msg")
}

fun kurwaInfo(msg: String) {
    println("[Curva]: $msg")
}

fun kurwaInternal(msg: String): Nothing {
    error("[Internal Curva Error]: $msg")
}

inline fun <T> tryNotNull(code: () -> T): T {
    return try {
        code()
    } catch (e: NullPointerException) {
        kurwaInternal("Should not be null")
    }
}
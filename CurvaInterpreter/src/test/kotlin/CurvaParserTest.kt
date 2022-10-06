package lakotka.anton.curva

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class CurvaParserTest {

    @Test
    fun `turn left and right should be ignored`() {
        val leftOrRight = setOf(CurvaToken.TurnLeft, CurvaToken.TurnRight)

        for (start in CurvaToken.values()) {
            if (start in leftOrRight) continue
            for (end in CurvaToken.values()) {
                if (end in leftOrRight) continue
                for (middle in leftOrRight) {
                    val expected = listOf(start, end).flatMap { it.turns }.tokenizeToBrainFuck()
                    val actualTurns = listOf(start, middle, end).flatMap { it.turns }
                    val actualTokens = actualTurns.tokenizeToBrainFuck()

                    assertEquals(
                        expected,
                        actualTokens,
                        "Combination of ${start.name} => ${middle.name} => ${end.name} fails"
                    )
                }
            }
        }
    }
}
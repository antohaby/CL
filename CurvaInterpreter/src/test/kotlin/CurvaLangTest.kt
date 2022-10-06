package lakotka.anton.curva

import java.io.File
import java.io.StringWriter
import kotlin.test.Test
import kotlin.test.assertEquals

class TestIO(
    val input: String = "",
) : Runner.IO {
    private val writer = StringWriter()
    private val reader = input.reader()

    val output get() = writer.toString()

    override fun read(): Byte {
        return reader.read().toByte()
    }

    override fun write(byte: Byte) {
        writer.write(byte.toInt())
    }
}

class CurvaLangTest {

    @Test
    fun testHelloWorldUpDown() = assertHelloWorld("hello_world_up_down.png")

    @Test
    fun testHelloWorldLeftRight() = assertHelloWorld("hello_world_left_right.png")

    @Test
    fun testHelloWorldRightLeft() = assertHelloWorld("hello_world_right_left.png")

    @Test
    fun testHelloWorldDownUp() = assertHelloWorld("hello_world_down_up.png")

    @Test
    fun testHelloWorldSpiral() = assertHelloWorld("hello_world_spiral.png")

    private fun assertHelloWorld(name: String) {
        val file = this.javaClass.getResource("/samples/$name").toURI().let { File(it) }
        val program = curvaLangProgram(file)

        val io = TestIO()
        val runner = Runner(
            program = program,
            io = io
        )

        runner.run()

        assertEquals("Hello World!\n", io.output)
    }
}
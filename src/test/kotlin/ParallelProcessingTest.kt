import org.junit.jupiter.api.Test

import org.assertj.core.api.Assertions
import utils.ParallelProcessing

class ParallelProcessingTest {
    @Test
    fun `should execute functions simultaneously`() {
        val inputs = listOf("a", "b", "c", "d")
        val outputs = ParallelProcessing().runAllInParallelBatches(3, inputs, this::doSomething)

        Assertions.assertThat(outputs).contains(
            "NOT a", "NOT b", "NOT c", "NOT d"
        )
        // Ensure the first 3 get processed together, so the 4th will be the last
        Assertions.assertThat(outputs.get(3)).isEqualTo("NOT d")
    }

    private fun doSomething(input: String): String {
        println("STARTING with $input")
        val output = "NOT $input"
        Thread.sleep(1000)
        println("ENDING with $output")
        return output
    }
}
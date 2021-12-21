package utils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class ParallelProcessing {
    fun <T, V> runAllInBatches(batchSize: Int, inputs: List<T>, block: (T) -> V): List<V> {
        val batchInputs = inputs.chunked(batchSize)
        val outputs = mutableListOf<V>()
        batchInputs.forEach { batch ->
            val inputOutputMapEntries = BatchMapEntries<T, V>(batch)
            runBatch(inputOutputMapEntries, block)
            outputs.addAll(inputOutputMapEntries.outputs)
        }
        return outputs
    }

    private fun <T, V> runBatch(inputOutputMapEntries: BatchMapEntries<T, V>, block: (T) -> V) {
        // Rudimentary batch
        runBlocking {
            inputOutputMapEntries.getInputs().forEach {
                launch(Dispatchers.Default) {
                    val output = block(it)
                    inputOutputMapEntries.addOutput(output)
                }
            }
        }
    }
}

class BatchMapEntries<T, V>(private val inputs: List<T>) {
    var outputs = mutableListOf<V>()

    @Synchronized fun addOutput(output: V) {
        outputs.add(output)
    }

    fun getInputs(): List<T> {
        return inputs
    }
}

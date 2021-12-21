package utils

import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class ParallelProcessing {
    fun <T, V> runAllInBatchesOf3(inputs: List<T>, block: (T) -> V): List<V> {
        val batchInputs = inputs.chunked(3)
        val outputs = mutableListOf<V>()
        batchInputs.forEach { batch ->
            val inputOutputMapEntries = BatchMapEntries<T, V>(batch)
            runBatch(inputOutputMapEntries, block)
            outputs.addAll(inputOutputMapEntries.outputs)
        }
        return outputs
    }

    fun <T, V> runBatch(inputOutputMapEntries: BatchMapEntries<T, V>, block: (T) -> V) {
        // Rudimentary batch
        runBlocking {
            inputOutputMapEntries.getInputs().forEach {
                launch {
                    val output = block(it)
                    inputOutputMapEntries.addOutput(output)
                }
            }
/*
            if (inputOutputMapEntries.hasInput(0)) {
                launch {
                    val output = block(inputOutputMapEntries.getInput(0))
                    inputOutputMapEntries.addOutput(output)
                }
            }
            if (inputOutputMapEntries.hasInput(1)) {
                launch {
                    val output = block(inputOutputMapEntries.getInput(1))
                    inputOutputMapEntries.addOutput(output)
                }
            }
            if (inputOutputMapEntries.hasInput(2)) {
                launch {
                    val output = block(inputOutputMapEntries.getInput(2))
                    inputOutputMapEntries.addOutput(output)
                }
            }
 */
        }
    }
}

class BatchMapEntries<T, V>(private val inputs: List<T>) {
    var outputs = mutableListOf<V>()

    fun hasInput(index: Int): Boolean {
        return inputs.size > index
    }

    fun getInput(index: Int): T {
        return inputs[index]
    }

    @Synchronized fun addOutput(output: V) {
        outputs.add(output)
    }

    fun getInputs(): List<T> {
        return inputs
    }
}

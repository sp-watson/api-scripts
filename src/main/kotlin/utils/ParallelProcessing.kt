package utils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.reflect.KFunction1

class ParallelProcessing {
    fun <T, V> runInBatches(batchSize: Int, inputs: Collection<T>, block: KFunction1<List<T>, List<V>>): List<V> {
        val batchInputs = inputs.chunked(batchSize)
        val outputs = mutableListOf<V>()
        batchInputs.forEach { batch ->
            val output = block(batch)
            outputs.addAll(output)
        }
        return outputs
    }

    fun <T, C, V> runAllInParallelBatches(batchSize: Int, inputs: Collection<T>, context: C, block: (T, C) -> V): List<V> {
        val batchInputs = inputs.chunked(batchSize)
        val outputs = mutableListOf<V>()
        batchInputs.forEach { batch ->
            val inputOutputMapEntries = BatchMapEntries<T, V>(batch)
            runAllInParallel(inputOutputMapEntries, context, block)
            outputs.addAll(inputOutputMapEntries.outputs)
        }
        return outputs
    }

    private fun <T, C, V> runAllInParallel(inputOutputMapEntries: BatchMapEntries<T, V>, context: C, block: (T, C) -> V) {
        // Rudimentary batch
        runBlocking {
            inputOutputMapEntries.getInputs().forEach {
                launch(Dispatchers.Default) {
                    val output = block(it, context)
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

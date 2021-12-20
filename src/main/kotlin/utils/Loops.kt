package utils

class Loops {
    fun doWhileWithDelay(timeoutInSecs: Long, delayInSecs: Long, evaluator: () -> Boolean): Boolean {
        var loopCount = 0
        var needsToBeTrue = false
        do {
            Thread.sleep(delayInSecs * 1000)
            needsToBeTrue = evaluator()
            loopCount++
        } while (!needsToBeTrue && loopCount < (timeoutInSecs / delayInSecs))
        return needsToBeTrue
    }
}
package rpmigration

import fileaccess.PrisonLookup
import spreadsheetaccess.SpreadsheetReader
import utils.ListProcessing
import utils.ParallelProcessing

class PrintPrisonResolutions (
    private val spreadsheetReader: SpreadsheetReader,
    private val prisonLookup: PrisonLookup,
) {
    fun handle() {
        println("Starting")

        val offenderInformation = spreadsheetReader.readAllRows()
        val uniquePrisonNames = ListProcessing().findUniquePrisonNames(offenderInformation)
        val prisonsWithLookup = prisonLookup.getResolvedPrisons()

        val failedResolutions = ListProcessing().resolveNames(uniquePrisonNames, prisonsWithLookup)
        ParallelProcessing().runAllInParallelBatches(1, failedResolutions, null, this::printFailedResolutions)

        println("Finished printing failed resolutions (${failedResolutions.size}) failed")
    }

    private fun printFailedResolutions(prisonName: String, context: Nothing?) {
        println("Failed to find a match for $prisonName")
    }
}
package rpmigration

import gateways.PrisonApi
import spreadsheetaccess.SpreadsheetReader
import utils.ListProcessing
import utils.ParallelProcessing
import utils.ResolutionResult

class PrintPrisonResolutionsUsingPrisonApi (
    private val spreadsheetReader: SpreadsheetReader,
    private val prisonApi: PrisonApi,
) {
    fun handle() {
        println("Starting")

        val offenderInformation = spreadsheetReader.readAllRows()
        val uniquePrisonNames = ListProcessing().findUniquePrisonNames(offenderInformation)
        val nomisPrisons = prisonApi.getAllInstitutions()

        val processedRowResults = ListProcessing().resolveNames(uniquePrisonNames, nomisPrisons)

        val failedResolutions = ListProcessing().extractFailedResolutions(processedRowResults)
        ParallelProcessing().runAllInParallelBatches(1, failedResolutions, null, this::printFailedResolutions)

        val successfulResolutions = ListProcessing().extractSuccessfulResolutions(processedRowResults)
        ParallelProcessing().runAllInParallelBatches(1, successfulResolutions, null, this::printSuccessfulResolutions)

        println("Finished printing ${processedRowResults.size} rows")
    }

    private fun printFailedResolutions(resolutionInformation: ResolutionResult, context: Nothing?) {
        println("Failed to find a match for ${resolutionInformation.initialName}: ${resolutionInformation.resolutionNotes}")
    }

    private fun printSuccessfulResolutions(resolutionInformation: ResolutionResult, context: Nothing?) {
        println("${resolutionInformation.initialName},${resolutionInformation.resolvedNomisCode}")
    }
}
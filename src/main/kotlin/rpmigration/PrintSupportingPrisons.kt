package rpmigration

import gateways.PrisonApi
import spreadsheetaccess.SpreadsheetReader
import utils.ListProcessing
import utils.ParallelProcessing

data class OffenderSupportingPrisonDetails (
    val offenderNo: String,
    // We ought to have an object error to represent errors and/or supporting prison information
    val supportingPrison: String,
    val foundSupportingPrison: Boolean,
)

class PrintSupportingPrisons (
    private val spreadsheetReader: SpreadsheetReader,
    private val prisonApi: PrisonApi,
) {
    fun handle() {
        println("Starting")

        val offenderInformation = spreadsheetReader.readAllRows()
        val validOffenderNos = ListProcessing().findValidOffenderNos(offenderInformation)

        val processedRows = ParallelProcessing().runInBatches(3, validOffenderNos, this::processOffendersBatch)

        val nullOffenderSize = offenderInformation.size - validOffenderNos.size
        val validOffenderProcessingFailed = ListProcessing().countFailed(processedRows)
        println("Finished printing. Total processed: ${validOffenderNos.size}, Processed failed: $validOffenderProcessingFailed. Missing offenderNos: $nullOffenderSize")
    }

    private fun processOffendersBatch(offenderNos: List<String>): List<Boolean> {
        try {
            val movementDetails = prisonApi.getLatestMovement(offenderNos)
            val lastReleasePrisonByOffender: List<OffenderSupportingPrisonDetails> = ListProcessing().getLastReleaseByOffender(offenderNos, movementDetails)
            return ParallelProcessing().runAllInParallelBatches(3, lastReleasePrisonByOffender, this::printSupportingPrison)
        } catch (th: Throwable) {
            // Consider giving better message
            val errorInformation = th.message ?: "No message"
            val offenderReleaseInformationWithError: List<OffenderSupportingPrisonDetails> = ListProcessing().makeOffenderReleaseInformationWithError(offenderNos, errorInformation)
            return ParallelProcessing().runAllInParallelBatches(3, offenderReleaseInformationWithError, this::printSupportingPrison)
        }
    }

    private fun printSupportingPrison(supportingPrisonDetails: OffenderSupportingPrisonDetails): Boolean {
        println("${supportingPrisonDetails.offenderNo} - ${supportingPrisonDetails.supportingPrison}")
        return supportingPrisonDetails.foundSupportingPrison
    }
}
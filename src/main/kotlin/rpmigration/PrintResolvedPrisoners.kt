package rpmigration

import gateways.PrisonApi
import spreadsheetaccess.SpreadsheetReader
import utils.ListProcessing
import utils.OffenderProcessing
import utils.ParallelProcessing
import java.time.LocalDate

data class OffenderDetails (
    val offenderNo: String?,
    val dob: LocalDate,
    val firstNames: String,
    val familyName: String,
)

data class ResolvedOffenderDetails (
    val originalDetails: OffenderDetails,
    // Consider converting to object
    val result: ResolutionResult,
    val matchingOffenderNo: String?,
    val processingError: String?
)

enum class ResolutionResult {
    FOUND, NOT_FOUND, MULTIPLE_MATCHES, NOT_MATCHING_PROVIDED_NO, PROCESSING_ERROR
}

class PrintResolvedPrisoners (
    private val spreadsheetReader: SpreadsheetReader,
    private val prisonApi: PrisonApi,
) {
    fun handle() {
        println("Starting")

        val offenderInformation = spreadsheetReader.readAllRows()
        val validOffenderNos = ListProcessing().getValidOffenderDetails(offenderInformation)

        val processedRows = ParallelProcessing().runAllInParallelBatches(3, validOffenderNos, null, this::processOffendersBatch)

        val nullOffenderSize = offenderInformation.size - validOffenderNos.size
        val errorCounts = ListProcessing().countFailedTypes(processedRows)
        println("Finished printing. Total processed: ${validOffenderNos.size}. Missing offenderNos: $nullOffenderSize, Not matched: ${errorCounts.noneMatched}, Multiple matches: ${errorCounts.multipleMatches}, Not matching provided: ${errorCounts.notMatchedProvided}, API error: ${errorCounts.errored}")
    }

    private fun processOffendersBatch(offenderDetails: OffenderDetails, context: Nothing?): ResolutionResult {
        try {
            val foundOffenderDetails = prisonApi.globalSearch(offenderDetails.familyName, offenderDetails.dob)
            val resolutionResult = OffenderProcessing().generateResolutionResult(offenderDetails, foundOffenderDetails)
            return printResolvedOffenderInformation(resolutionResult)
        } catch (th: Throwable) {
            val erroredResolutionResult = OffenderProcessing().generateErroredResolutionResult(offenderDetails, th)
            return printResolvedOffenderInformation(erroredResolutionResult)
        }
    }

    private fun printResolvedOffenderInformation(resolvedInformation: ResolvedOffenderDetails): ResolutionResult {
        val resolvedSummary = OffenderProcessing().generateOffenderResolutionSummaryString(resolvedInformation)
        println("${resolvedInformation.originalDetails.offenderNo}: $resolvedSummary (${resolvedInformation.originalDetails.familyName}, ${resolvedInformation.originalDetails.firstNames} - ${resolvedInformation.originalDetails.dob})")
        return resolvedInformation.result
    }
}
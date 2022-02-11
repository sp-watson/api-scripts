package utils

import gateways.OffenderSearchResult
import rpmigration.OffenderDetails
import rpmigration.OffenderInformation
import rpmigration.ResolutionResult
import rpmigration.ResolvedOffenderDetails
import spreadsheetaccess.RowInformation

class OffenderProcessing {
    fun filterMigratedOffenders(
        offenderInformation: List<RowInformation>,
        successfullyMigratedOffenders: List<String>
    ): List<RowInformation> {
        return offenderInformation.filter { !successfullyMigratedOffenders.contains(it.offenderNo) }
    }

    fun cleanseOffenderInformation(
        spreadsheetRowInformation: List<RowInformation>,
    ): List<OffenderInformation> {
        return spreadsheetRowInformation.map { OffenderInformation(it.offenderNo ?: "OFFNONOTSET", it.prisonName ?: "PRISONNAMENOTSET", it.hospitalNomsId ?: "Unknown") }
    }

    fun generateResolutionResult(offenderDetails: OffenderDetails, foundOffenderDetails: List<OffenderSearchResult>): ResolvedOffenderDetails {
        if (foundOffenderDetails.isEmpty()) {
            return ResolvedOffenderDetails(offenderDetails, ResolutionResult.NOT_FOUND, null, null)
        }
        if (foundOffenderDetails.size > 1) {
            return ResolvedOffenderDetails(offenderDetails, ResolutionResult.MULTIPLE_MATCHES, null, null)
        }
        val foundOffender = foundOffenderDetails[0]
        if (offenderDetails.offenderNo != null && foundOffender.offenderNo != offenderDetails.offenderNo) {
            return ResolvedOffenderDetails(offenderDetails, ResolutionResult.NOT_MATCHING_PROVIDED_NO, null, null)
        }
        return ResolvedOffenderDetails(offenderDetails, ResolutionResult.FOUND, foundOffender.offenderNo, null)
    }

    fun generateErroredResolutionResult(offenderDetails: OffenderDetails, th: Throwable): ResolvedOffenderDetails {
        // Consider giving better message
        val errorInformation = th.message ?: "No message"
        return ResolvedOffenderDetails(offenderDetails, ResolutionResult.PROCESSING_ERROR, null, errorInformation)
    }

    fun generateOffenderResolutionSummaryString(resolvedInformation: ResolvedOffenderDetails): String {
        return when (resolvedInformation.result) {
            ResolutionResult.FOUND -> "MATCH: ${resolvedInformation.matchingOffenderNo}"
            ResolutionResult.NOT_FOUND -> "NO MATCH FOUND"
            ResolutionResult.MULTIPLE_MATCHES -> "MULTIPLE MATCHES"
            ResolutionResult.NOT_MATCHING_PROVIDED_NO -> "NOT MATCHING PROVIDED OFFENDER NO"
            ResolutionResult.PROCESSING_ERROR -> "ERROR [${resolvedInformation.processingError}]"
        }
    }

    @Throws(exceptionClasses = [PrisonNotResolvedException::class])
    fun resolvePrisonName(prisonIdsByName: Map<String, String>, prisonName: String): String {
        val foundPrisonId = prisonIdsByName[prisonName]
        return foundPrisonId ?: throw PrisonNotResolvedException()
    }
}

class PrisonNotResolvedException : RuntimeException("Prison name not resolved")
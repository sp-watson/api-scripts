package utils

import gateways.AgencyDetails
import gateways.MovementInformation
import rpmigration.OffenderDetails
import rpmigration.OffenderSupportingPrisonDetails
import rpmigration.ResolutionResult
import spreadsheetaccess.RowInformation

data class FailureTypeCounts (
    val noneMatched: Int,
    val multipleMatches: Int,
    val notMatchedProvided: Int,
    val errored: Int,
)

data class ResolutionResult (
    val initialName: String,
    val resolvedNomisCode: String?,
    val resolutionNotes: String?,
)

class ListProcessing {
    fun findUniquePrisonNames(rows: List<RowInformation>): Set<String> {
        return rows.filter { it.prisonName != null }.map { it.prisonName!! }.toSet()
    }

    fun findValidOffenderNos(rows: List<RowInformation>): List<String> {
        return rows.filter { it.offenderNo != null }.map { it.offenderNo!! }
    }

    fun getValidOffenderDetails(offenderInformation: List<RowInformation>): List<OffenderDetails> {
        return offenderInformation.filter { it.dob != null && it.firstName != null && it.familyName != null }
            .map { OffenderDetails(it.offenderNo, it.dob!!, it.firstName!!, it.familyName!!) }
    }

    fun countFailed(outputResult: List<Boolean>): Int {
        return outputResult.filter { !it }.count()
    }

    fun countFailedTypes(outputResult: List<ResolutionResult>): FailureTypeCounts {
        val groupedErrors = outputResult.groupBy { it }
        return FailureTypeCounts(
            groupedErrors[ResolutionResult.NOT_FOUND]?.size ?: 0,
            groupedErrors[ResolutionResult.MULTIPLE_MATCHES]?.size ?: 0,
            groupedErrors[ResolutionResult.NOT_MATCHING_PROVIDED_NO]?.size ?: 0,
            groupedErrors[ResolutionResult.PROCESSING_ERROR]?.size ?: 0,
        )
    }

    fun getLastReleaseByOffender(offenderNos: List<String>, movementDetails: List<MovementInformation>): List<OffenderSupportingPrisonDetails> {
        val lastMovementsByOffender = movementDetails.groupBy { it.offenderNo }
        return offenderNos.map { Pair(it, lastMovementsByOffender[it]) }.map { getOffenderLastMovementDetails(it) }
    }

    fun makeOffenderReleaseInformationWithError(offenderNos: List<String>, errorInformation: String): List<OffenderSupportingPrisonDetails> {
        return offenderNos.map { OffenderSupportingPrisonDetails(it, errorInformation, false) }
    }

    private fun getOffenderLastMovementDetails(offenderMovements: Pair<String, List<MovementInformation>?>): OffenderSupportingPrisonDetails {
        if (offenderMovements.second == null) {
            return OffenderSupportingPrisonDetails(offenderMovements.first, "Missing last movement", false)
        }
        if (offenderMovements.second!!.size > 1) {
            return OffenderSupportingPrisonDetails(offenderMovements.first, "Too many movements", false)
        }
        val lastMovement = offenderMovements.second!![0]
        if ("OUT" != lastMovement.toAgency) {
            return OffenderSupportingPrisonDetails(offenderMovements.first, "Last movement was not to OUT", false)
        }
        if ("REL" != lastMovement.movementType) {
            return OffenderSupportingPrisonDetails(offenderMovements.first, "Last movement was not REL", false)
        }
        return OffenderSupportingPrisonDetails(offenderMovements.first, lastMovement.fromAgency, true)
    }

    fun resolveNames(namesToMatch: Collection<String>, namesAvailable: List<AgencyDetails>): List<utils.ResolutionResult> {
        return resolve(namesToMatch,
            {
                val uniqueNames = it.replace("HMP", "").replace("YOI", "").replace("HMYOI", "").replace("zzz", "").replace("(", "").replace(")", "").replace("/", "").replace("&", "")
                val nameToSearch = uniqueNames.trim().uppercase()
                nameToSearch
            },
            {
                namesAvailable.filter { v -> v.description.uppercase().contains(it) }.map { it.agencyId }
            })
    }

    fun resolve(namesToMatch: Collection<String>, transformer: (String) -> String, resolver: (s: String) -> List<String>): List<utils.ResolutionResult> {
        val resolutionResults = mutableListOf<utils.ResolutionResult>()

        // Do basic matching for the moment
        namesToMatch.forEach {
            val nameToSearch = transformer(it)
            // Remove the common names
            // val uniqueNames = it.replace("HMP", "").replace("YOI", "").replace("HMYOI", "").replace("zzz", "").replace("(", "").replace(")", "").replace("/", "").replace("&", "")
            // val nameToSearch = uniqueNames.trim().uppercase()
            val foundValues = resolver(nameToSearch)
            if (foundValues.size < 1) {
                resolutionResults.add(
                    ResolutionResult(
                        initialName = it,
                        resolvedNomisCode = null,
                        resolutionNotes = "Nothing found",
                    )
                )
            }
            else if (foundValues.size > 1) {
                resolutionResults.add(
                    ResolutionResult(
                        initialName = it,
                        resolvedNomisCode = null,
                        resolutionNotes = "Multiple found",
                    )
                )
            }
            else {
                resolutionResults.add(
                    ResolutionResult(
                        initialName = it,
                        resolvedNomisCode = foundValues[0],
                        resolutionNotes = null,
                    )
                )
            }
        }

        return resolutionResults
    }

    fun extractFailedResolutions(processedRowResults: List<utils.ResolutionResult>): List<utils.ResolutionResult> {
         return processedRowResults.filter { it.resolvedNomisCode == null }
    }

    fun extractSuccessfulResolutions(processedRowResults: List<utils.ResolutionResult>): List<utils.ResolutionResult> {
        return processedRowResults.filter { it.resolvedNomisCode != null }
    }

    fun resolveNames(namesToMatch: Collection<String>, namesAvailable: Map<String, String>): List<String> {
        val matchedOrEmptyNames = namesToMatch.map { Pair(it, namesAvailable.getOrDefault(it, "")) }
        val emptyOnes = matchedOrEmptyNames.filter { it.second.isEmpty() }
        return emptyOnes.map { it.first }
    }
}
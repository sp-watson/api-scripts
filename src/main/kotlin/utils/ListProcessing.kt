package utils

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

class ListProcessing {
    fun findUniqueHospitals(rows: List<RowInformation>): Set<String> {
        return rows.filter { it.hospitalName != null }.map { it.hospitalName!! }.toSet()
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
}
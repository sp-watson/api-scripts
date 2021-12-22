package rpmigration

import gateways.PrisonApi
import gateways.PrisonerNotAddedToRpDbException
import gateways.PrisonerNotRemovedFromRpDbException
import gateways.RestrictedPatientsApi
import gateways.ServerException
import gateways.WebClientException
import output.PerOffenderFileOutput
import output.StreamReference
import utils.Loops
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

data class MigrateOffenderRequestEvent(
    val responsiblePrisonId: String,
    val offenderNo: String,
    val targetHospitalCode: String,
    val progressStreamRef: StreamReference,
)

class MigrateOffender (
    private val progressStream: PerOffenderFileOutput,
    private val prisonApi: PrisonApi,
    private val restrictedPatientApi: RestrictedPatientsApi,
    private val removingExistingRestrictedPatient: Boolean,
    private val recallMovementReasonCode: String,
    private val recallImprisonmentStatus: String,
    private val recallIsYouthOffender: Boolean,
    private val dischargeToHospitalCommentText: String
) {
    @Throws(exceptionClasses = [WebClientException::class, ServerException::class, PrisonerNotRemovedFromRpDbException::class])
    fun handle(command: MigrateOffenderRequestEvent) {
        println("Attempting to migrate ${command.offenderNo} ")

        val recallTime = LocalDateTime.now()
        prisonApi.recall(command.responsiblePrisonId, command.offenderNo, recallTime,
            recallMovementReasonCode, recallImprisonmentStatus, recallIsYouthOffender)
        progressStream.recallSuccessful(command.progressStreamRef)

        if (removingExistingRestrictedPatient) {
            val offenderRemoved = checkPrisonerRemoved(command.offenderNo)
            if (!offenderRemoved) {
                throw PrisonerNotRemovedFromRpDbException()
            }
        }

        val movementTime = LocalDateTime.now()
        restrictedPatientApi.moveToHospital(command.responsiblePrisonId, command.offenderNo, command.targetHospitalCode, movementTime, dischargeToHospitalCommentText)

        val offenderAdded = checkPrisonerAdded(command.offenderNo)
        if (!offenderAdded) {
            throw PrisonerNotAddedToRpDbException()
        }
    }

    private fun checkPrisonerRemoved(offenderNo: String): Boolean {
            // Need to poll to wait for the recall to take affect
            println("Waiting for prisoner to be removed from the RP DB: $offenderNo ")
            val offenderRemoved = Loops().doWhileWithDelay(180, 2) {
                try {
                    restrictedPatientApi.getRestrictedPatient(offenderNo)
                    println(
                        "${
                            LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_TIME)
                        } Prisoner is there.. Still waiting... for $offenderNo "
                    )
                } catch (wce: WebClientException) {
                    if (wce.isNotFound()) {
                        return@doWhileWithDelay true
                    }
                }
                false
            }
        return offenderRemoved
    }

    private fun checkPrisonerAdded(offenderNo: String): Boolean {
        // Need to poll to wait for the recall to take affect
        println("Waiting for prisoner to be added to the RP DB: $offenderNo ")
        val offenderAdded = Loops().doWhileWithDelay(90, 5) {
            try {
                val response = restrictedPatientApi.getRestrictedPatient(offenderNo)
                if (response.contains(offenderNo)) {
                    return@doWhileWithDelay true
                }
            } catch (wce: WebClientException) {
            }
            println(
                "${
                    LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_TIME)
                } Prisoner is still not there.. Still waiting... for $offenderNo "
            )
            false
        }
        return offenderAdded
    }
}
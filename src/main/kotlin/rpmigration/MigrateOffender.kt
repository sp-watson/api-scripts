package rpmigration

import gateways.PrisonerNotAddedToRpDbException
import gateways.PrisonerNotRemovedFromRpDbException
import gateways.RestrictedPatientsApi
import gateways.ServerException
import gateways.WebClientException
import fileaccess.SuccessfulOffenderMigrations
import utils.Loops
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

data class MigrateOffenderRequestEvent(
    val offenderNo: String,
    val targetHospitalNomsId: String,
)

class MigrateOffender (
    private val successfulMigrations: SuccessfulOffenderMigrations,
    private val restrictedPatientApi: RestrictedPatientsApi,
) {
    @Throws(exceptionClasses = [WebClientException::class, ServerException::class, PrisonerNotRemovedFromRpDbException::class])
    fun handle(command: MigrateOffenderRequestEvent) {
        println("Attempting to migrate ${command.offenderNo} ")

        restrictedPatientApi.migrateInOffender(command.offenderNo, command.targetHospitalNomsId)

        val offenderAdded = checkPrisonerAdded(command.offenderNo)
        if (!offenderAdded) {
            throw PrisonerNotAddedToRpDbException()
        }

        successfulMigrations.offenderMigrated(command.offenderNo)
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
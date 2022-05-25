package rpmigration

import fileaccess.PerOffenderFileOutput
import fileaccess.SuccessfulOffenderMigrations
import fileaccess.SynchronisedSummaryOutput
import spreadsheetaccess.SpreadsheetReader
import utils.OffenderProcessing
import utils.ParallelProcessing
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

data class MigrationRequestEvent (
    val startRow: Int,
    val numberOfRows: Int,
)

data class OffenderInformation(
    val offenderNo: String,
    val prisonName: String,
    val hospitalNomsId: String,
)

class Migrations (
    private val spreadsheetReader: SpreadsheetReader,
    private val successfulMigrations: SuccessfulOffenderMigrations,
    private val resultStream: SynchronisedSummaryOutput,
    private val progressStream: PerOffenderFileOutput,
    private val migrateOffenderCommand: MigrateOffender,
) {
    fun handle(command: MigrationRequestEvent) {
        val startTime = LocalDateTime.now()
        println("Reading from row ${command.startRow} - ${command.numberOfRows} rows")

        val offenderInformation = spreadsheetReader.readRows(command.startRow, command.numberOfRows)
        val successfullyMigratedOffenders = successfulMigrations.getMigratedOffenders()
        val unmigratedOffenderInformation = OffenderProcessing().filterMigratedOffenders(offenderInformation, successfullyMigratedOffenders)
        val actualOffenderInformation = OffenderProcessing().cleanseOffenderInformation(unmigratedOffenderInformation)

        val failedItems = ParallelProcessing().runAllInParallelBatches(
            4,
            actualOffenderInformation,
            null,
            this::migrateOffender)

        val failedItemList = failedItems.filter { !it }
        val endTime = LocalDateTime.now()
        val totalSeconds = ChronoUnit.SECONDS.between(startTime, endTime)
        println("Read ${offenderInformation.size} items in $totalSeconds seconds - ${unmigratedOffenderInformation.size} processed, ${failedItemList.size} failed")
    }

    private fun migrateOffender(migrationInfo: OffenderInformation, context: Nothing?): Boolean {
        val progressStreamRef = progressStream.migrationStarted(migrationInfo.offenderNo)
        if (progressStreamRef == null) {
            resultStream.logMigrationResult(migrationInfo.offenderNo, false)
            return false
        }
        try {
            val migrationData = MigrateOffenderRequestEvent(migrationInfo.offenderNo, migrationInfo.hospitalNomsId)
            migrateOffenderCommand.handle(migrationData)
        } catch (th: Throwable) {
            resultStream.logMigrationResult(migrationInfo.offenderNo, false)
            progressStream.migrationFailed(progressStreamRef, th)
            return false
        }
        resultStream.logMigrationResult(migrationInfo.offenderNo, true)
        progressStream.migrationSucceeded(progressStreamRef)
        return true
    }
}
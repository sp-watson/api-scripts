package rpmigration

import fileaccess.PerOffenderFileOutput
import fileaccess.SuccessfulOffenderMigrations
import fileaccess.SynchronisedSummaryOutput
import spreadsheetaccess.RowInformation
import spreadsheetaccess.SpreadsheetReader
import utils.OffenderProcessing
import utils.ParallelProcessing

data class MigrationRequestEvent (
    val startRow: Int,
    val numberOfRows: Int,
)

class Migrations (
    private val spreadsheetReader: SpreadsheetReader,
    private val successfulMigrations: SuccessfulOffenderMigrations,
    private val resultStream: SynchronisedSummaryOutput,
    private val progressStream: PerOffenderFileOutput,
    private val migrateOffenderCommand: MigrateOffender,
) {
    fun handle(command: MigrationRequestEvent) {
        println("Reading from row ${command.startRow} - ${command.numberOfRows} rows")

        val offenderInformation = spreadsheetReader.readRows(command.startRow, command.numberOfRows)
        val successfullyMigratedOffenders = successfulMigrations.getMigratedOffenders()
        val unmigratedOffenderInformation = OffenderProcessing().filterMigratedOffenders(offenderInformation, successfullyMigratedOffenders)
        val failedItems = ParallelProcessing().runAllInBatches(3, unmigratedOffenderInformation, this::migrateOffender)

        val failedItemList = failedItems.filter { !it }
        println("Read ${offenderInformation.size} items - ${unmigratedOffenderInformation.size} processed, ${failedItemList.size} failed")
    }

    private fun migrateOffender(migrationInfo: RowInformation): Boolean {
        val progressStreamRef = progressStream.migrationStarted(migrationInfo.offenderNo)
        val migrationData = MigrateOffenderRequestEvent(migrationInfo.prisonId, migrationInfo.offenderNo, migrationInfo.targetHospitalCode, progressStreamRef)
        try {
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
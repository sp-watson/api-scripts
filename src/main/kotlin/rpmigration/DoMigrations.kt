package rpmigration

import fileaccess.PerOffenderFileOutput
import fileaccess.PrisonLookup
import fileaccess.SuccessfulOffenderMigrations
import fileaccess.SynchronisedSummaryOutput
import spreadsheetaccess.SpreadsheetReader
import utils.OffenderProcessing
import utils.ParallelProcessing

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
    private val prisonLookup: PrisonLookup,
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
        val actualOffenderInformation = OffenderProcessing().cleanseOffenderInformation(unmigratedOffenderInformation)

        val prisonIdsByName = prisonLookup.getResolvedPrisons()

        val failedItems = ParallelProcessing().runAllInParallelBatches(3, actualOffenderInformation, prisonIdsByName, this::migrateOffender)

        val failedItemList = failedItems.filter { !it }
        println("Read ${offenderInformation.size} items - ${unmigratedOffenderInformation.size} processed, ${failedItemList.size} failed")
    }

    private fun migrateOffender(migrationInfo: OffenderInformation, prisonIdsByName: Map<String, String>): Boolean {
        val progressStreamRef = progressStream.migrationStarted(migrationInfo.offenderNo)
        try {
            val prisonId = OffenderProcessing().resolvePrisonName(prisonIdsByName, migrationInfo.prisonName)
            val migrationData = MigrateOffenderRequestEvent(prisonId, migrationInfo.offenderNo, migrationInfo.hospitalNomsId, progressStreamRef)
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
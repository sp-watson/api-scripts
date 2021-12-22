import gateways.PrisonApi
import gateways.RestrictedPatientsApi
import fileaccess.PerOffenderFileOutput
import fileaccess.SuccessfulOffenderMigrations
import fileaccess.SynchronisedSummaryOutput
import rpmigration.MigrateOffender
import rpmigration.MigrationRequestEvent
import rpmigration.Migrations
import spreadsheetaccess.SpreadsheetReader

fun main() {
    val config: Config = Dev()

    val archiveSubDirectory = "archive"
    val existingSuccessfulMigrationFileName = "SUCCESSFUL_MIGRATIONS.txt"
    val recallMovementReasonCode = "Z"
    val recallImprisonmentStatus = "RECEP_HOS"
    val recallIsYouthOffender = false
    val dischargeToHospitalCommentText = "Released to hospital (Restricted Patients migration)"

    val successfulOffenderMigrations = SuccessfulOffenderMigrations(config.resultsBaseDirectory, existingSuccessfulMigrationFileName)
    val progressStream = PerOffenderFileOutput(config.resultsBaseDirectory, archiveSubDirectory)
    val prisonApi = PrisonApi(config.prisonApiRootUrl, config.token)
    val rpApi = RestrictedPatientsApi(config.restrictedPatientsApiRootUrl, config.token)

    val migrationCommand = Migrations(
        SpreadsheetReader(config.spreadsheetFileName),
        successfulOffenderMigrations,
        SynchronisedSummaryOutput(config.resultsBaseDirectory),
        PerOffenderFileOutput(config.resultsBaseDirectory, archiveSubDirectory),
        MigrateOffender(successfulOffenderMigrations, progressStream, prisonApi, rpApi, config.removingExistingRestrictedPatient,
            recallMovementReasonCode, recallImprisonmentStatus, recallIsYouthOffender, dischargeToHospitalCommentText)
    )

    migrationCommand.handle(MigrationRequestEvent(11, 4))
}
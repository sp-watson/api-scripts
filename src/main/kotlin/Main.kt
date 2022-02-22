import gateways.PrisonApi
import gateways.RestrictedPatientsApi
import fileaccess.PerOffenderFileOutput
import fileaccess.PrisonLookup
import fileaccess.SuccessfulOffenderMigrations
import fileaccess.SuccessfulOffenderRecalls
import fileaccess.SynchronisedSummaryOutput
import rpmigration.MigrateOffender
import rpmigration.MigrationRequestEvent
import rpmigration.Migrations
import spreadsheetaccess.SpreadsheetReader

fun main() {
    // ====== MUST DO ON VPN ========
    val config: Config = Prod()

    val firstOffenderNumber = 720
    val numberOfOffenders = 20

    val archiveSubDirectory = "archive"
    val existingSuccessfulMigrationsFileName = "SUCCESSFUL_MIGRATIONS.txt"
    val existingSuccessfulRecallsFileName = "SUCCESSFUL_RECALLS.txt"
    val recallMovementReasonCode = "Z"
    val recallImprisonmentStatus = null
    val recallIsYouthOffender = false
    val dischargeToHospitalCommentText = "Released to hospital (Restricted Patients migration)"

    val successfulOffenderMigrations = SuccessfulOffenderMigrations(config.resultsBaseDirectory, existingSuccessfulMigrationsFileName)
    val successfulOffenderRecalls = SuccessfulOffenderRecalls(config.resultsBaseDirectory, existingSuccessfulRecallsFileName)
    val progressStream = PerOffenderFileOutput(config.resultsBaseDirectory, archiveSubDirectory)
    val prisonApi = PrisonApi(config.prisonApiRootUrl, config.token)
    val prisonLookup = PrisonLookup(config.prisonLookupFileName)
    val rpApi = RestrictedPatientsApi(config.restrictedPatientsApiRootUrl, config.token)

    val migrationCommand = Migrations(
        SpreadsheetReader(config.spreadsheetFileName),
        prisonLookup,
        successfulOffenderMigrations,
        successfulOffenderRecalls,
        SynchronisedSummaryOutput(config.resultsBaseDirectory),
        PerOffenderFileOutput(config.resultsBaseDirectory, archiveSubDirectory),
        MigrateOffender(successfulOffenderMigrations, successfulOffenderRecalls, progressStream, prisonApi, rpApi, config.removingExistingRestrictedPatient,
            recallMovementReasonCode, recallImprisonmentStatus, recallIsYouthOffender, dischargeToHospitalCommentText)
    )

    migrationCommand.handle(MigrationRequestEvent(firstOffenderNumber, numberOfOffenders))
}
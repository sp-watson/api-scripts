import gateways.PrisonApi
import gateways.RestrictedPatientsApi
import fileaccess.PerOffenderFileOutput
import fileaccess.PrisonLookup
import fileaccess.SuccessfulOffenderMigrations
import fileaccess.SynchronisedSummaryOutput
import rpmigration.MigrateOffender
import rpmigration.MigrationRequestEvent
import rpmigration.Migrations
import spreadsheetaccess.SpreadsheetReader

fun main() {
    // ====== MUST DO ON VPN ========
    val config: Config = PreProd()

    val firstOffenderNumber = 9
    val numberOfOffenders = 5

    val archiveSubDirectory = "archive"
    val existingSuccessfulMigrationFileName = "SUCCESSFUL_MIGRATIONS.txt"
    val recallMovementReasonCode = "Z"
    val recallImprisonmentStatus = "RECEP_HOS"
    val recallIsYouthOffender = false
    val dischargeToHospitalCommentText = "Released to hospital (Restricted Patients migration)"

    val successfulOffenderMigrations = SuccessfulOffenderMigrations(config.resultsBaseDirectory, existingSuccessfulMigrationFileName)
    val progressStream = PerOffenderFileOutput(config.resultsBaseDirectory, archiveSubDirectory)
    val prisonApi = PrisonApi(config.prisonApiRootUrl, config.token)
    val prisonLookup = PrisonLookup(config.prisonLookupFileName)
    val rpApi = RestrictedPatientsApi(config.restrictedPatientsApiRootUrl, config.token)

    val migrationCommand = Migrations(
        SpreadsheetReader(config.spreadsheetFileName),
        prisonLookup,
        successfulOffenderMigrations,
        SynchronisedSummaryOutput(config.resultsBaseDirectory),
        PerOffenderFileOutput(config.resultsBaseDirectory, archiveSubDirectory),
        MigrateOffender(successfulOffenderMigrations, progressStream, prisonApi, rpApi, config.removingExistingRestrictedPatient,
            recallMovementReasonCode, recallImprisonmentStatus, recallIsYouthOffender, dischargeToHospitalCommentText)
    )

    migrationCommand.handle(MigrationRequestEvent(firstOffenderNumber, numberOfOffenders))
}
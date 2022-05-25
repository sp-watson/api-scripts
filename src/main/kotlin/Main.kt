import gateways.RestrictedPatientsApi
import fileaccess.PerOffenderFileOutput
import fileaccess.SuccessfulOffenderMigrations
import fileaccess.SynchronisedSummaryOutput
import rpmigration.MigrateOffender
import rpmigration.MigrationRequestEvent
import rpmigration.Migrations
import spreadsheetaccess.SpreadsheetReader

fun main() {
    // ====== MUST DO ON VPN ========
    val config: Config = PreProd()

    val firstOffenderNumber = 162
    val numberOfOffenders = 5

    val archiveSubDirectory = "archive"
    val existingSuccessfulMigrationsFileName = "SUCCESSFUL_MIGRATIONS.txt"
    val existingAlreadyInPrisonFileName = "ALREADY_IN_PRISON.txt"

    val successfulOffenderMigrations = SuccessfulOffenderMigrations(config.resultsBaseDirectory, existingSuccessfulMigrationsFileName, existingAlreadyInPrisonFileName)
    val rpApi = RestrictedPatientsApi(config.restrictedPatientsApiRootUrl, config.token)

    val migrationCommand = Migrations(
        SpreadsheetReader(config.spreadsheetFileName),
        successfulOffenderMigrations,
        SynchronisedSummaryOutput(config.resultsBaseDirectory),
        PerOffenderFileOutput(config.resultsBaseDirectory, archiveSubDirectory),
        MigrateOffender(successfulOffenderMigrations, rpApi)
    )

    migrationCommand.handle(MigrationRequestEvent(firstOffenderNumber, numberOfOffenders))
}
import gateways.RestrictedPatientsApi
import fileaccess.PerOffenderFileOutput
import fileaccess.SuccessfulOffenderMigrations
import rpmigration.MigrateOffender
import rpmigration.SingleTestMigration
import rpmigration.SingleTestMigrationRequestEvent

fun main() {
    // ====== MUST DO ON VPN ========
    val config: Config = PreProd()

    val archiveSubDirectory = "singlemigrationarchive"
    val existingSuccessfulMigrationsFileName = "SUCCESSFUL_MIGRATIONS.txt"
    val existingAlreadyInPrisonFileName = "ALREADY_IN_PRISON.txt"

    val offenderNo = "??"
    val hospitalNomsId = "??"

    val successfulOffenderMigrations = SuccessfulOffenderMigrations(config.resultsBaseDirectory, existingSuccessfulMigrationsFileName, existingAlreadyInPrisonFileName)
    val rpApi = RestrictedPatientsApi(config.restrictedPatientsApiRootUrl, config.token)

    val migrationCommand = SingleTestMigration(
        PerOffenderFileOutput(config.resultsBaseDirectory, archiveSubDirectory),
        MigrateOffender(successfulOffenderMigrations, rpApi)
    )

    migrationCommand.handle(SingleTestMigrationRequestEvent(offenderNo, hospitalNomsId))
}
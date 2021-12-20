import gateways.PrisonApi
import gateways.RestrictedPatientsApi
import output.ProgressStream
import output.ResultStream
import rpmigration.MigrateOffender
import rpmigration.MigrationRequestEvent
import rpmigration.Migrations
import spreadsheetaccess.SpreadsheetReader

fun main(args: Array<String>) {
    val removingExistingRestrictedPatient = true
    val prisonApiRootUrl = "[API URL HERE]"
    val restrictedPatientsApiRootUrl = "[API URL HERE]"
    val token = """
        [TOKEN HERE]
    """.trimIndent()
    val spreadsheetFileName = "[SPREADSHEET FILENAME HERE]"
    val resultsBaseDirectory = "[DIRECTORY HERE]"

    val archiveSubDirectory = "archive"
    val progressStream = ProgressStream(resultsBaseDirectory, archiveSubDirectory)
    val migrationCommand = Migrations(
        SpreadsheetReader(spreadsheetFileName),
        ResultStream(resultsBaseDirectory),
        ProgressStream(resultsBaseDirectory, archiveSubDirectory),
        MigrateOffender(progressStream, PrisonApi(prisonApiRootUrl, token), RestrictedPatientsApi(restrictedPatientsApiRootUrl, token), removingExistingRestrictedPatient)
    )

    migrationCommand.handle(MigrationRequestEvent(1, 1))
}
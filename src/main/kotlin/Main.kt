import gateways.PrisonApi
import gateways.RestrictedPatientsApi
import output.PerOffenderFileOutput
import output.SynchronisedSummaryFileOutput
import rpmigration.MigrateOffender
import rpmigration.MigrationRequestEvent
import rpmigration.Migrations
import spreadsheetaccess.SpreadsheetReader

fun main() {
    val config: Config = Dev()

    val archiveSubDirectory = "archive"
    val recallMovementReasonCode = "Z"
    val recallImprisonmentStatus = "RECEP_HOS"
    val recallIsYouthOffender = false
    val dischargeToHospitalCommentText = "Released to hospital (Restricted Patients migration)"

    val progressStream = PerOffenderFileOutput(config.resultsBaseDirectory, archiveSubDirectory)
    val prisonApi = PrisonApi(config.prisonApiRootUrl, config.token)
    val rpApi = RestrictedPatientsApi(config.restrictedPatientsApiRootUrl, config.token)

    val migrationCommand = Migrations(
        SpreadsheetReader(config.spreadsheetFileName),
        SynchronisedSummaryFileOutput(config.resultsBaseDirectory),
        PerOffenderFileOutput(config.resultsBaseDirectory, archiveSubDirectory),
        MigrateOffender(progressStream, prisonApi, rpApi, config.removingExistingRestrictedPatient,
            recallMovementReasonCode, recallImprisonmentStatus, recallIsYouthOffender, dischargeToHospitalCommentText)
    )

    migrationCommand.handle(MigrationRequestEvent(11, 4))
}
import gateways.PrisonApi
import rpmigration.PrintSupportingPrisons
import spreadsheetaccess.SpreadsheetReader

fun main() {
    val config: Config = PreProdWithMovementClientCreds()

    val prisonApi = PrisonApi(config.prisonApiRootUrl, config.token)

    val supportingPrisonCommand = PrintSupportingPrisons(
        SpreadsheetReader(config.spreadsheetFileName),
        prisonApi
    )

    supportingPrisonCommand.handle()
}
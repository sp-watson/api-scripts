import gateways.PrisonApi
import rpmigration.PrintPrisonResolutions
import rpmigration.PrintPrisonResolutionsUsingPrisonApi
import spreadsheetaccess.SpreadsheetReader

fun main() {
    val config: Config = PreProd()

    val prisonApi = PrisonApi(config.prisonApiRootUrl, config.token)

    val resolutionCommand = PrintPrisonResolutionsUsingPrisonApi(
        SpreadsheetReader(config.spreadsheetFileName),
        prisonApi,
    )

    resolutionCommand.handle()
}
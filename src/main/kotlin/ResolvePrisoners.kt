import gateways.PrisonApi
import rpmigration.PrintResolvedPrisoners
import spreadsheetaccess.SpreadsheetReader

fun main() {
    val config: Config = PreProdWithMovementClientCreds()

    val prisonApi = PrisonApi(config.prisonApiRootUrl, config.token)

    val supportingPrisonCommand = PrintResolvedPrisoners(
        SpreadsheetReader(config.spreadsheetFileName),
        prisonApi
    )

    supportingPrisonCommand.handle()
}
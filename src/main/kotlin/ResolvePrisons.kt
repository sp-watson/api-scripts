import fileaccess.PrisonLookup
import gateways.PrisonApi
import rpmigration.PrintPrisonResolutions
import spreadsheetaccess.SpreadsheetReader

fun main() {
    val config: Config = PreProd()

    val prisonLookup = PrisonLookup(config.prisonLookupFileName)

    val resolutionCommand = PrintPrisonResolutions(
        SpreadsheetReader(config.spreadsheetFileName),
        prisonLookup,
    )

    resolutionCommand.handle()
}
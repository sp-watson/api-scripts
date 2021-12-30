import rpmigration.PrintUniqueHospitals
import spreadsheetaccess.SpreadsheetReader

fun main() {
    val config: Config = PreProd()

    val migrationCommand = PrintUniqueHospitals(
        SpreadsheetReader(config.spreadsheetFileName),
    )

    migrationCommand.handle()
}
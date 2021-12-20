package spreadsheetaccess

data class RowInformation(
    val prisonId: String,
    val offenderNo: String,
    val targetHospitalCode: String,
)

class SpreadsheetReader(
    val fileName: String
) {
    fun readRows(startRow: Int, numberOfRows: Int): List<RowInformation> {
        // Test Data
        return listOf(RowInformation("MDI", "G8161GV", "RAMTON"))
    }
}

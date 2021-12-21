package spreadsheetaccess

data class RowInformation(
    val prisonId: String,
    val offenderNo: String,
    val targetHospitalCode: String,
)

class SpreadsheetReader(
    val fileName: String
) {
    companion object {
        private const val WORKSHEET_NAME = "s45a abd 47 with matched in nom"
        private val OFFENDER_NO_ROW = "J"
        private val OFFENDER_NO_START_COL = 2
    }

    fun readRows(startRow: Int, numberOfRows: Int): List<RowInformation> {
        var foundRows = mutableListOf<RowInformation>()
        XlsxParser(fileName, WORKSHEET_NAME).onlineParse { cell ->
            if (!cell.contents.isBlank() && isWantedCell(cell.rowNumber, startRow, numberOfRows)) {
                if (cell.columnName == OFFENDER_NO_ROW) {
                    println("\nFound cell at ${cell.columnName} ${cell.rowNumber}:  ${cell.contents}")
                    foundRows.add(RowInformation("MDI", cell.contents, "ALPHA"))
                }
            }
        }
        return foundRows
    }

    private fun isWantedCell(rowNumber: Int, startRow: Int, numberOfRows: Int): Boolean {
        // Should pre-compute these
        val firstRow = (OFFENDER_NO_START_COL + startRow - 1)
        val lastRow = firstRow + numberOfRows - 1
        return rowNumber in firstRow..lastRow
    }
}

package spreadsheetaccess

import java.util.function.BiFunction

data class RowInformation(
    val offenderNo: String?,
    val hospitalName: String?,
)

class SpreadsheetReader(
    val fileName: String
) {
    companion object {
        private const val WORKSHEET_NAME = "s45 and s47"
        private val OFFENDER_NO_COL = "A"
        private val HOSPITAL_NAME_COL = "D"
        private val OFFENDER_NO_START_ROW = 2
    }

    fun readRows(startRow: Int, numberOfRows: Int): List<RowInformation> {
        val readRowData = mutableMapOf<Int, RowInformation>()
        XlsxParser(fileName, WORKSHEET_NAME).onlineParse { cell ->
            if (isWantedRow(cell.rowNumber, startRow, numberOfRows)) {
                // We ought to abstract the column processing - the column names are in 2 places
                // Also, this code is a bit long-winded. We ought to refactor
                if (cell.columnName.equals(OFFENDER_NO_COL) || cell.columnName.equals(HOSPITAL_NAME_COL)) {
                    println("\nFound cell at ${cell.columnName} ${cell.rowNumber}:  ${cell.contents}")
                    readRowData.compute(cell.rowNumber,
                        BiFunction<Int, RowInformation?, RowInformation?> { key, oldData ->
                            var offenderNo = oldData?.offenderNo
                            var hospitalName = oldData?.hospitalName
                            if ("#N/A".equals(offenderNo)) {
                                offenderNo = null
                            }
                            // Consider using when!
                            if (cell.columnName.equals(OFFENDER_NO_COL)) {
                                offenderNo = cell.contents
                            } else if (cell.columnName.equals(HOSPITAL_NAME_COL)) {
                                hospitalName = cell.contents
                            }
                            return@BiFunction RowInformation(offenderNo, hospitalName)
                        })
                }
            }
        }
        // This is the quickest way to convert a MutableCollection to a List - consider making a Collection
        return readRowData.values.distinct()
    }

    private fun isWantedRow(rowNumber: Int, startRow: Int, numberOfRows: Int): Boolean {
        // Should pre-compute these
        val firstRow = (OFFENDER_NO_START_ROW + startRow - 1)
        val lastRow = firstRow + numberOfRows - 1
        return rowNumber in firstRow..lastRow
    }
}

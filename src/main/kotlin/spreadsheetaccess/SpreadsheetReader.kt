package spreadsheetaccess

import java.lang.Long.parseLong
import java.time.LocalDate
import java.util.function.BiFunction

data class RowInformation(
    val offenderNo: String?,
    val hospitalNomsId: String?,
    val prisonName: String?,
    val dob: LocalDate?,
    val firstName: String?,
    val familyName: String?,
)

class SpreadsheetReader(
    val fileName: String
) {
    companion object {
        private const val WORKSHEET_NAME = "S45a and S47 matched data"
        private val OFFENDER_NO_COL = "E"
        private val HOSPITAL_NOMS_ID_COL = "J"
        private val PRISON_NAME_COL = "G"
        private val DATE_OF_BIRTH_COL = "D"
        private val FIRST_NAMES_COL = "C"
        private val FAMILY_NAME_COL = "B"
        private val OFFENDER_NO_START_ROW = 1
    }

    fun readAllRows(): List<RowInformation> {
        return readRows(1, 2000) // Assume 2000 for now
    }

    fun readRows(startRow: Int, numberOfRows: Int): List<RowInformation> {
        val readRowData = mutableMapOf<Int, RowInformation>()
        XlsxParser(fileName, WORKSHEET_NAME).onlineParse { cell ->
            if (isWantedRow(cell.rowNumber, startRow, numberOfRows)) {
                // We ought to abstract the column processing - the column names are in 2 places
                // Also, this code is a bit long-winded. We ought to refactor
                if (cell.columnName == OFFENDER_NO_COL ||
                    cell.columnName == HOSPITAL_NOMS_ID_COL ||
                    cell.columnName == PRISON_NAME_COL ||
                    cell.columnName == DATE_OF_BIRTH_COL ||
                    cell.columnName == FIRST_NAMES_COL ||
                    cell.columnName == FAMILY_NAME_COL
                ) {
                    println("\nFound cell at ${cell.columnName} ${cell.rowNumber}:  ${cell.contents}")
                    readRowData.compute(cell.rowNumber,
                        BiFunction<Int, RowInformation?, RowInformation?> { key, oldData ->
                            var offenderNo = oldData?.offenderNo
                            var hospitalNomsId = oldData?.hospitalNomsId
                            var prisonName = oldData?.prisonName
                            var dob = oldData?.dob
                            var firstName = oldData?.firstName
                            var familyName = oldData?.familyName
                            when (cell.columnName) {
                                OFFENDER_NO_COL -> {
                                    offenderNo = cell.contents
                                    if ("#N/A" == offenderNo) {
                                        offenderNo = null
                                    }
                                }
                                HOSPITAL_NOMS_ID_COL -> {
                                    hospitalNomsId = cell.contents
                                }
                                PRISON_NAME_COL -> {
                                    prisonName = cell.contents
                                }
                                DATE_OF_BIRTH_COL -> {
                                    try {
                                        val daysSinceStartOf1900 = parseLong(cell.contents) - 2
                                        dob = LocalDate.of(1900, 1, 1).plusDays(daysSinceStartOf1900)
                                    } catch (nfe: NumberFormatException) {
                                        // Do nothing - it will be null
                                    }
                                }
                                FIRST_NAMES_COL -> {
                                    firstName = cell.contents
                                }
                                FAMILY_NAME_COL -> {
                                    familyName = cell.contents
                                }
                            }
                            return@BiFunction RowInformation(offenderNo, hospitalNomsId, prisonName, dob, firstName, familyName)
                        })
                }
            }
        }
        return readRowData.values.toList()
    }

    private fun isWantedRow(rowNumber: Int, startRow: Int, numberOfRows: Int): Boolean {
        // Should pre-compute these
        val firstRow = (OFFENDER_NO_START_ROW + startRow - 1)
        val lastRow = firstRow + numberOfRows - 1
        return rowNumber in firstRow..lastRow
    }
}

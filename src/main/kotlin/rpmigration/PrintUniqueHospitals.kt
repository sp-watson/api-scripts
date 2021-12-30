package rpmigration

import spreadsheetaccess.SpreadsheetReader
import utils.ListProcessing
import utils.ParallelProcessing

class PrintUniqueHospitals (
    private val spreadsheetReader: SpreadsheetReader,
) {
    fun handle() {
        println("Starting")

        val offenderInformation = spreadsheetReader.readAllRows()
        val hospitalDescriptions = ListProcessing().findUniqueHospitals(offenderInformation)

        val processedRows = ParallelProcessing().runAllInParallelBatches(3, hospitalDescriptions, this::printHospital)

        println("Finished printing ${processedRows.size} rows")
    }

    private fun printHospital(hospitalDescription: String) {
        println("$hospitalDescription")
    }
}
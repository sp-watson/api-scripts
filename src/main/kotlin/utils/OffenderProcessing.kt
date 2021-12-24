package utils

import rpmigration.OffenderInformation
import spreadsheetaccess.RowInformation

class OffenderProcessing {
    fun filterMigratedOffenders(
        offenderInformation: List<RowInformation>,
        successfullyMigratedOffenders: List<String>
    ): List<RowInformation> {
        return offenderInformation.filter { !successfullyMigratedOffenders.contains(it.offenderNo) }
    }

    fun cleanseOffenderInformation(
        spreadsheetRowInformation: List<RowInformation>,
    ): List<OffenderInformation> {
        return spreadsheetRowInformation.map { OffenderInformation(it.offenderNo ?: "A1234AA", "MDI", it.hospitalName ?: "Unknown") }
    }
}
package utils

import spreadsheetaccess.RowInformation

class OffenderProcessing {
    fun filterMigratedOffenders(
        offenderInformation: List<RowInformation>,
        successfullyMigratedOffenders: List<String>
    ): List<RowInformation> {
        return offenderInformation.filter { !successfullyMigratedOffenders.contains(it.offenderNo) }
    }
}
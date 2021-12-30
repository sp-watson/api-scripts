import org.junit.jupiter.api.Test

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import spreadsheetaccess.RowInformation
import spreadsheetaccess.SpreadsheetReader
import java.time.LocalDate

class SpreadsheetTest {
    private lateinit var underTest: SpreadsheetReader

    @BeforeEach
    fun setup() {
        underTest = SpreadsheetReader(getAbsoluteFileName("Example_data_v2.xlsx"))
    }

    @Test
    fun `should read spreadsheet`() {
        val foundData = underTest.readRows(4, 3)

        Assertions.assertThat(foundData).contains(
            RowInformation( "A8344DX", "Cygnet Hospital Woking", LocalDate.of(1968, 1, 19), "Simon", "Kirk"),
            RowInformation( "A2388EK", "John Howard Centre", LocalDate.of(1991, 10, 15), "Sahme", "Mohamed"),
            RowInformation( null, "Wellesley Hospital", LocalDate.of(1990, 10, 3), "Jamie David", "Groves"),
        )
    }

    private fun getAbsoluteFileName(resourceFileName: String): String {
        return javaClass.getResource("/$resourceFileName").file
    }
}
import org.junit.jupiter.api.Test

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import spreadsheetaccess.RowInformation
import spreadsheetaccess.SpreadsheetReader

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
            RowInformation( "A8344DX", "Cygnet Hospital Woking"),
            RowInformation( "A2388EK", "John Howard Centre"),
            RowInformation( null, "Wellesley Hospital"),
        )
    }

    private fun getAbsoluteFileName(resourceFileName: String): String {
        return javaClass.getResource("/$resourceFileName").file
    }
}
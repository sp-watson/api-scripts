import org.junit.jupiter.api.Test

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import spreadsheetaccess.RowInformation
import spreadsheetaccess.SpreadsheetReader

class SpreadsheetTest {
    private lateinit var underTest: SpreadsheetReader

    @BeforeEach
    fun setup() {
        underTest = SpreadsheetReader(getAbsoluteFileName("Example_data.xlsx"))
    }

    @Test
    fun `should read spreadsheet`() {
        val foundData = underTest.readRows(1, 2)

        Assertions.assertThat(foundData).contains(
            RowInformation("MDI", "A0007CL", "ALPHA"),
            RowInformation("MDI", "A0013EJ", "ALPHA")
        )
    }

    private fun getAbsoluteFileName(resourceFileName: String): String {
        return javaClass.getResource("/$resourceFileName").file
    }
}
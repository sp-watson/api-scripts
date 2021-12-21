package output

import java.nio.file.Path
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class SynchronisedSummaryFileOutput(baseDirectory: String) {
    // Might be more reliable to store the File object here
    private val path: Path

    init {
        val uniqueTimeString = LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME).replace(':', '_')
        this.path = Path.of(baseDirectory, "SUMMARY-$uniqueTimeString.txt")
    }

    @Synchronized fun logMigrationResult(offenderNo: String, successful: Boolean) {
        val summaryString = if (successful) "SUCCEEDED" else "FAILED"
        path.toFile().appendText("$offenderNo - $summaryString\n" )
    }
}
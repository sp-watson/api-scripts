package fileaccess

import java.nio.file.Path
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class SynchronisedSummaryOutput(baseDirectory: String) {
    // Might be more reliable to store the File object here
    private val path: Path
    private var totalProcessed = 0

    init {
        val uniqueTimeString = LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME).replace(':', '_')
        this.path = Path.of(baseDirectory, "SUMMARY-$uniqueTimeString.txt")
    }

    @Synchronized fun logMigrationResult(offenderNo: String, successful: Boolean) {
        val summaryString = if (successful) "SUCCEEDED" else "FAILED"
        totalProcessed++
        path.toFile().appendText("$offenderNo - $summaryString ($totalProcessed processed so far)\n" )
    }
}
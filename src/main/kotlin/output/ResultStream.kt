package output

import java.io.File
import java.nio.file.Path
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class ResultStream(private val baseDirectory: String) {
    fun logFailedMigration(offenderNo: String) {
        getFile().writeText("Failed: $offenderNo" )
    }

    private fun getFile(): File {
        val uniqueTimeString = LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME).replace(':', '_')
        return Path.of(baseDirectory, "FAILURES-$uniqueTimeString.txt").toFile()
    }
}
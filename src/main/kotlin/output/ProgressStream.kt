package output

import java.io.File
import java.io.FileWriter
import java.io.PrintWriter
import java.nio.file.Path
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

typealias StreamReference = FileWriter

class ProgressStream(val baseDirectory: String, val archiveSubDirectory: String) {
    fun migrationStarted(offenderNo: String): StreamReference {
        val fileToWriteTo = getFile(offenderNo)
        if (fileToWriteTo.exists()) {
            val isMoved: Boolean = fileToWriteTo.renameTo(getArchiveFile(offenderNo))
            if (!isMoved) {
                throw RuntimeException("Unable to rename file $")
            }
        }

        val fileWriter = FileWriter(getFile(offenderNo), true)
        fileWriter.write("Starting migration for offender $offenderNo\n")
        return fileWriter
    }

    fun recallSuccessful(streamRef: StreamReference) {
        streamRef.write("RECALL SUCCEEDED\n")
    }

    fun migrationFailed(streamRef: StreamReference, th: Throwable) {
        // Will close
        streamRef.use { w ->
            w.write("\nFAILED: ${th.message}\n\n")
            th.printStackTrace(PrintWriter(w, true))
        }
    }

    fun migrationSucceeded(streamRef: StreamReference) {
        // Will close
        streamRef.use { w ->  w.write("SUCCEEDED\n") }
    }

    private fun getFile(offenderNo: String): File {
        return Path.of(baseDirectory, "$offenderNo.txt").toFile()
    }

    private fun getArchiveFile(offenderNo: String): File {
        val uniqueTimeString = LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME)
        val uniqueTimeString2 = uniqueTimeString.replace(':', '_')
        return Path.of(baseDirectory, archiveSubDirectory, "$offenderNo-$uniqueTimeString2.txt").toFile()
    }
}
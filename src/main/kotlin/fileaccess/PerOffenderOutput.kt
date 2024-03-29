package fileaccess

import java.io.File
import java.io.FileWriter
import java.io.PrintWriter
import java.nio.file.Path
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

typealias StreamReference = FileWriter

class PerOffenderFileOutput(val baseDirectory: String, val archiveSubDirectory: String) {
    fun migrationStarted(offenderNo: String): StreamReference? {
        val fileToWriteTo = getFile(offenderNo)
        if (fileToWriteTo.exists()) {
            val archiveFile = getArchiveFile(offenderNo)
            val isMoved: Boolean = fileToWriteTo.renameTo(archiveFile)
            if (!isMoved) {
                System.err.println("Unable to rename file for ${archiveFile.absolutePath}")
                return null
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
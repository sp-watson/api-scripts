package fileaccess

import java.io.File
import java.io.FileWriter
import java.io.PrintWriter
import java.nio.file.Path
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class SuccessfulOffenderMigrations(val baseDirectory: String, val name: String) {
    fun getMigratedOffenders(): List<String> {
        return getFile().readLines()
    }

    @Synchronized fun offenderMigrated(offenderNo: String) {
        getFile().appendText("$offenderNo\n")
    }

    private fun getFile(): File {
        return Path.of(baseDirectory, name).toFile()
    }
}
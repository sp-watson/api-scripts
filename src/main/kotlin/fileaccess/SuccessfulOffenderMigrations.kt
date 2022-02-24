package fileaccess

import java.io.File
import java.nio.file.Path

class SuccessfulOffenderMigrations(val baseDirectory: String, val name: String, val alreadyInPrisonFileName: String) {
    fun getMigratedOffenders(): List<String> {
        val successfullyMigratedOffenderLines = getFile().readLines()
        val alreadyInPrisonOffenderLines = getAlreadyInPrisonFile().readLines()
        return listOf(successfullyMigratedOffenderLines, alreadyInPrisonOffenderLines).flatten()
    }

    @Synchronized fun offenderMigrated(offenderNo: String) {
        getFile().appendText("$offenderNo\n")
    }

    private fun getFile(): File {
        return Path.of(baseDirectory, name).toFile()
    }

    private fun getAlreadyInPrisonFile(): File {
        return Path.of(baseDirectory, alreadyInPrisonFileName).toFile()
    }
}
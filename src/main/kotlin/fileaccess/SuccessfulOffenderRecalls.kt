package fileaccess

import java.io.File
import java.nio.file.Path

class SuccessfulOffenderRecalls(val baseDirectory: String, val name: String) {
    fun getRecalledOffenders(): List<String> {
        return getFile().readLines()
    }

    @Synchronized fun offenderRecalled(offenderNo: String) {
        getFile().appendText("$offenderNo\n")
    }

    private fun getFile(): File {
        return Path.of(baseDirectory, name).toFile()
    }
}
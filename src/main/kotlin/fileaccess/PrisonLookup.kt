package fileaccess

import java.io.File
import java.nio.file.Path

class PrisonLookup(val name: String) {
    fun getResolvedPrisons(): Map<String, String> {
        val foundLookups = HashMap<String, String>()
        val commaSeparatedListOfPrisonNamesAndNomisCode = getFile().readLines()
        commaSeparatedListOfPrisonNamesAndNomisCode.forEach {
            val separatedLine = it.split(",")
            if (separatedLine.size > 1) {
                foundLookups.put(
                    separatedLine[0],
                    separatedLine[1]
                )
            } else {
                System.err.println("Error reading line: $separatedLine")
            }
        }
        return foundLookups
    }

    private fun getFile(): File {
        return Path.of(name).toFile()
    }
}
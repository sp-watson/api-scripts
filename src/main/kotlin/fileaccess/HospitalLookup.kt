package fileaccess

import java.io.File
import java.nio.file.Path

class HospitalLookup(val name: String) {
    fun getResolvedHospitals(): Map<String, String> {
        val foundLookups = HashMap<String, String>()
        val commaSeparatedListOfHospitalNameTypeAndNomisCode = getFile().readLines()
        commaSeparatedListOfHospitalNameTypeAndNomisCode.forEach {
            val separatedLine = it.split(",")
            if (separatedLine.size > 2) {
                foundLookups.put(
                    separatedLine[0],
                    separatedLine[2]
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
package gateways

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.Serializable
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Serializable
data class MovementInformation (
    val offenderNo: String,
    val fromAgency: String,
    val toAgency: String?,
    val movementType: String,
)

@Serializable
data class AgencyDetails (
    val agencyId: String,
    val description: String,
    val longDescription: String,
    val active: Boolean,
)

@Serializable
data class OffenderSearchResult (
    val offenderNo: String,
    val firstName: String,
    val latestLocationId: String,
)

class PrisonApi (
    val prisonApiUrl: String,
    val token: String,
) {
    // Synchronized to avoid "Error: This resource is currently locked by another user." errors
    @Throws(exceptionClasses = [WebClientException::class, ServerException::class])
    @Synchronized fun recall(toPrisonId: String,
                                            offenderNo: String,
                                            movementTimeString: LocalDateTime,
                                            movementReasonCode: String,
                                            imprisonmentStatus: String,
                                            isYouthOffender: Boolean) {
        println("Attempting to recall $offenderNo ")

        val youthOffenderString = if (isYouthOffender) "true" else "false"
        val data = """
        {
          "prisonId": "$toPrisonId",
          "movementReasonCode": "$movementReasonCode",
          "recallTime": "$movementTimeString",
          "imprisonmentStatus": "$imprisonmentStatus",
          "youthOffender": $youthOffenderString
        }
    """
        val conn = URL("$prisonApiUrl/api/offenders/$offenderNo/recall").openConnection() as HttpURLConnection
        conn.requestMethod = "PUT"
        conn.doOutput = true
        conn.setRequestProperty("Content-Type", "application/json")
        conn.setRequestProperty("Authorization", "Bearer $token")
        conn.outputStream.write(data.toByteArray(Charsets.UTF_8))
        try {
            val lines = conn.inputStream.use {
                it.bufferedReader().readLines()
            }
            lines.forEach{
                println("Line: $it")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            val errorInfo = conn.errorStream.use {
                it.bufferedReader().readLines()
            }
            val errorString = errorInfo.joinToString { it }
            errorInfo.forEach{
                println("Error Line: $it")
            }
            println("Error response: ${conn.responseCode}: ${conn.responseMessage}")
            if (conn.responseCode >= 500) {
                throw ServerException()
            }
            throw WebClientException(conn.responseCode, errorString)
        } finally {
            conn.disconnect()
        }
    }

    @Throws(exceptionClasses = [WebClientException::class, ServerException::class])
    fun getLatestMovement(offenderNos: List<String>): List<MovementInformation> {
        val data = offenderNos.joinToString (separator = "\", \"", prefix = "[\"", postfix = "\"]") { it }
        val conn = URL("$prisonApiUrl/api/movements/offenders?latestOnly=true&allBookings=true").openConnection() as HttpURLConnection
        conn.requestMethod = "POST"
        conn.doOutput = true
        conn.setRequestProperty("Content-Type", "application/json")
        conn.setRequestProperty("Authorization", "Bearer $token")
        conn.outputStream.write(data.toByteArray(Charsets.UTF_8))
        try {
            val lines = conn.inputStream.use {
                it.bufferedReader().readLines()
            }
            lines.forEach{
                println("Line: $it")
            }
            val combinedResult = lines.joinToString (" ")
            return Json {ignoreUnknownKeys = true} .decodeFromString(combinedResult)
        } catch (e: Exception) {
            e.printStackTrace()
            val errorString = getError(conn)
            println("Error response: ${conn.responseCode}: ${conn.responseMessage}")
            if (conn.responseCode >= 500) {
                throw ServerException()
            }
            throw WebClientException(conn.responseCode, errorString)
        } finally {
            conn.disconnect()
        }
    }

    @Throws(exceptionClasses = [WebClientException::class, ServerException::class])
    fun getAllInstitutions(): List<AgencyDetails> {
        val conn = URL("$prisonApiUrl/api/agencies/type/INST").openConnection() as HttpURLConnection
        conn.requestMethod = "GET"
        conn.doOutput = true
        conn.setRequestProperty("Authorization", "Bearer $token")
        try {
            val lines = conn.inputStream.use {
                it.bufferedReader().readLines()
            }
            lines.forEach{
                println("Line: $it")
            }
            val combinedResult = lines.joinToString (" ")
            return Json {ignoreUnknownKeys = true} .decodeFromString(combinedResult)
        } catch (e: Exception) {
            e.printStackTrace()
            val errorString = getError(conn)
            println("Error response: ${conn.responseCode}: ${conn.responseMessage}")
            if (conn.responseCode >= 500) {
                throw ServerException()
            }
            throw WebClientException(conn.responseCode, errorString)
        } finally {
            conn.disconnect()
        }
    }

    @Throws(exceptionClasses = [WebClientException::class, ServerException::class])
    fun globalSearch(lastName: String, dob: LocalDate): List<OffenderSearchResult> {
        val dobString = dob.format(DateTimeFormatter.ISO_DATE)
        val encodedLastName = URLEncoder.encode(lastName, "utf-8")
        val conn = URL("$prisonApiUrl/api/prisoners?lastName=$encodedLastName&dob=$dobString").openConnection() as HttpURLConnection
        conn.requestMethod = "GET"
        conn.doOutput = true
        conn.setRequestProperty("Authorization", "Bearer $token")
        try {
            val lines = conn.inputStream.use {
                it.bufferedReader().readLines()
            }
            lines.forEach{
                println("Line: $it")
            }
            val combinedResult = lines.joinToString (" ")
            return Json {ignoreUnknownKeys = true} .decodeFromString(combinedResult)
        } catch (e: Exception) {
            e.printStackTrace()
            val errorString = getError(conn)
            println("Error response: ${conn.responseCode}: ${conn.responseMessage}")
            if (conn.responseCode >= 500) {
                throw ServerException()
            }
            throw WebClientException(conn.responseCode, errorString)
        } finally {
            conn.disconnect()
        }
    }

    private fun getError(conn: HttpURLConnection): String {
        if (conn.errorStream != null) {
            val errorInfo = conn.errorStream.use {
                it.bufferedReader().readLines()
            }
            val errorString = errorInfo.joinToString { it }
            errorInfo.forEach {
                println("Error Line: $it")
            }
            return errorString
        }
        return "No error"
    }
}
package gateways

import java.net.HttpURLConnection
import java.net.URL
import java.time.LocalDateTime

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
}
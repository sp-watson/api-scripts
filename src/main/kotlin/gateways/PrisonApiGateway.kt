package gateways

import java.net.HttpURLConnection
import java.net.URL
import java.time.LocalDateTime

class PrisonApi (
    val prisonApiUrl: String,
    val token: String,
) {
    @Throws(exceptionClasses = [WebClientException::class, ServerException::class])
    fun recall(toPrisonId: String,
               offenderNo: String,
               movementTimeString: LocalDateTime) {
        println("Attempting to recall $offenderNo ")

        val data = """
        {
          "prisonId": "$toPrisonId",
          "movementReasonCode": "24",
          "recallTime": "$movementTimeString",
          "imprisonmentStatus": "CUR_ORA",
          "youthOffender": false
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
package gateways

import java.net.HttpURLConnection
import java.net.URL
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class RestrictedPatientsApi (
    val restrictedPatientsApiUrl: String,
    val token: String,
) {
    @Throws(exceptionClasses = [WebClientException::class, ServerException::class])
    fun moveToHospital(fromPrisonId: String,
                       offenderNo: String,
                       hospitalCode: String,
                       dischargeTime: LocalDateTime) {
        println("Attempting to move $offenderNo to hospital")

        val dischargeTimeString = dischargeTime.format(DateTimeFormatter.ISO_DATE_TIME)
        val data = """
        {
            "offenderNo": "$offenderNo",
            "hospitalLocationCode": "$hospitalCode",
            "dischargeTime": "$dischargeTimeString",
            "commentText": "Released to hospital (migration)",
            "supportingPrisonId": "$fromPrisonId",
            "fromLocationId": "$fromPrisonId"
        }
    """
        val conn = URL("$restrictedPatientsApiUrl/discharge-to-hospital").openConnection() as HttpURLConnection
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
    fun getRestrictedPatient(offenderNo: String): String {
        val conn = URL("$restrictedPatientsApiUrl/restricted-patient/prison-number/$offenderNo").openConnection() as HttpURLConnection
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
            return lines.joinToString (" ")
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
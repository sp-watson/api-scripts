package gateways

import java.net.HttpURLConnection
import java.net.URL

class RestrictedPatientsApi (
    val restrictedPatientsApiUrl: String,
    val token: String,
) {
    @Throws(exceptionClasses = [WebClientException::class, ServerException::class])
    fun migrateInOffender(offenderNo: String,
                       hospitalNomsId: String) {
        println("Attempting to migrate in $offenderNo")

        val data = """
        {
            "offenderNo": "$offenderNo",
            "hospitalLocationCode": "$hospitalNomsId"
        }
    """
        val conn = URL("$restrictedPatientsApiUrl/migrate-in-restricted-patient").openConnection() as HttpURLConnection
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
            var errorString = "No error information - see logs"
            if (conn.errorStream != null) {
                val errorInfo = conn.errorStream.use {
                    it.bufferedReader().readLines()
                }
                errorString = errorInfo.joinToString { it }
                errorInfo.forEach {
                    println("Error Line: $it")
                }
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
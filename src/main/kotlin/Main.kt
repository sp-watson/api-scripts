import java.net.HttpURLConnection
import java.net.URL
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

fun main(args: Array<String>) {
    println("Attempting to recall")

    val movementTimeString = LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME)
    val offenderNo = "[e.g. A1234AA]"
    val token = """
        [Token here]
    """.trimIndent()
    val data = """
        {
          "prisonId": "MDI",
          "movementReasonCode": "24",
          "recallTime": "$movementTimeString",
          "imprisonmentStatus": "CUR_ORA",
          "youthOffender": false
        }
    """
    val conn = URL("https://api-dev.prison.service.justice.gov.uk/api/offenders/$offenderNo/recall").openConnection() as HttpURLConnection
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
        errorInfo.forEach{
            println("Error Line: $it")
        }
    }
}
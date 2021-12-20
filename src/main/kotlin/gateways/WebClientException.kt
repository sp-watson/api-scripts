package gateways

class WebClientException (private val apiCode: Int, private val apiResponseBody: String?) : RuntimeException("API code: $apiCode , message: $apiResponseBody") {
    fun isNotFound(): Boolean {
        return apiCode == 404
    }
}
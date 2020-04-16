package ru.beryukhov.client_lib.http

import io.ktor.client.HttpClient
import io.ktor.client.features.ResponseException
import ru.beryukhov.common.model.*

open class BaseHttpClient {
    companion object {
        const val HEADER_CONTENT_TYPE = "Content-Type"
        const val HEADER_JSON = "application/json"
    }

    suspend inline fun <T> HttpClient.makeRequest(
        logMessage: String,
        log: suspend (String) -> Unit,
        block: HttpClient.() -> Success<T>
    ): Result<T> {
        log("\nSending request: $logMessage")
        try {

            val result = block.invoke(this)
            log(
                "Received result: ${result.value}"
            )
            return result

        } catch (e: ResponseException) {
            log(
                "Received error response: status = ${e.response.status}"
            )
            return Failure(
                HttpError(
                    e.response.status.value
                )
            )
        }
    }

    suspend inline fun HttpClient.makeCompletableRequest(
        logMessage: String,
        log: suspend (String) -> Unit,
        block: HttpClient.() -> CompletableResult
    ): CompletableResult {
        log("\nSending request: $logMessage")
        try {
            val result = block.invoke(this)
            log(
                "Received success result"
            )
            return result
        } catch (e: ResponseException) {
            log(
                "Received error response: status = ${e.response.status}"
            )
            return CompletableFailure(
                HttpError(e.response.status.value)
            )
        }
    }
}
package ru.beryukhov.client_lib.http

import io.ktor.client.HttpClient
import ru.beryukhov.common.model.Entity


expect fun createHttpClient(logRequests: Boolean): HttpClient

class HttpClientRepositoryImpl(private val serverUrl: String, logRequests: Boolean, private val log: suspend (String) -> Unit) :
    HttpClientRepository {

    private val httpClient: HttpClient by lazy { createHttpClient(logRequests) }

    override val clientApi: ClientApi<Entity> by lazy {
        ClientApiImpl(
            httpClient,
            serverUrl,
            log
        )
    }

}
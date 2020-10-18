package ru.koy.service

import io.ktor.client.*
import io.ktor.client.engine.apache.*
import io.ktor.client.features.json.*
import io.ktor.client.request.*
import io.ktor.http.*
import ru.koy.model.dto.JsonRpcRequest
import ru.koy.model.dto.JsonRpcResponse
import ru.koy.model.dto.UsernameLogin
import ru.koy.util.ScramUtil
import java.time.LocalTime

class AuthenticationService {
    private val client = HttpClient(Apache) {
        install(JsonFeature)
    }

    suspend fun registration(data: UsernameLogin): JsonRpcResponse {
        val rpcRequest = JsonRpcRequest(
            "AuthService.registration",
            listOf(data.username, data.password),
            LocalTime.now().toNanoOfDay()
        )

        return sendRequest(rpcRequest)
    }

    suspend fun login(data: UsernameLogin): JsonRpcResponse {
        val requestSalt = requestSalt(data.username)
        return requestSalt
    }

    private suspend fun requestSalt(username: String): JsonRpcResponse {
        val rpcRequest = JsonRpcRequest(
            "AuthService.getSaltByUsername",
            listOf(username, ScramUtil.toBase64(ScramUtil.generateSalt())),
            LocalTime.now().toNanoOfDay()
        )

        return sendRequest(rpcRequest)
    }

    private suspend fun sendRequest(rpcRequest: JsonRpcRequest): JsonRpcResponse {
        return client.request("http://localhost:8081/") {
            body = rpcRequest
            method = HttpMethod.Post
            contentType(ContentType.Application.Json)
        }
    }
}
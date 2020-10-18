package ru.koy.service

import com.google.gson.internal.LinkedTreeMap
import io.ktor.client.*
import io.ktor.client.engine.apache.*
import io.ktor.client.features.json.*
import io.ktor.client.request.*
import io.ktor.http.*
import ru.koy.model.dto.JsonRpcRequest
import ru.koy.model.dto.JsonRpcResponse
import ru.koy.model.dto.SaltResponse
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
        val clientNonce = ScramUtil.toBase64(ScramUtil.generateSalt())
        val requestSalt = requestSalt(data.username, clientNonce)
        val result = requestSalt.result ?: return requestSalt
        val saltResponse = getSaltedResponse(result)

        val authMessage = ScramUtil.getAuthMessage(
            ScramUtil.getClientFirstMessage(data.username, clientNonce),
            ScramUtil.getServerFirstMessage(saltResponse.rand, saltResponse.salt, saltResponse.i),
            ScramUtil.getClientFinalMessageWithoutProof(saltResponse.rand)
        )

        val clientProof = ScramUtil.getClientProof(
            data.password, authMessage,
            ScramUtil.fromBase64(saltResponse.salt), saltResponse.i
        )

        val rpcRequest = JsonRpcRequest(
            "AuthService.authenticate",
            listOf(saltResponse.rand, ScramUtil.toBase64(clientProof)),
            LocalTime.now().toNanoOfDay()
        )

        return sendRequest(rpcRequest)
    }

    private fun getSaltedResponse(result: Any): SaltResponse {
        val map = result as LinkedTreeMap<*, *>
        return SaltResponse(
            map["salt"] as String,
            (map["i"] as Double).toInt(),
            map["rand"] as String
        )
    }

    private suspend fun requestSalt(username: String, clientNonce: String): JsonRpcResponse {
        val rpcRequest = JsonRpcRequest(
            "AuthService.getSaltByUsername",
            listOf(username, clientNonce),
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

package ru.koy.model.dto

data class JsonRpcResponse(
    val result: Any? = null,
    val error: Map<String, Any?>? = null,
    val id: Long? = null,
    val jsonrpc: String = "2.0"
)

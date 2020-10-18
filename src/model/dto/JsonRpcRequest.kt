package ru.koy.model.dto

data class JsonRpcRequest(
    val method: String,
    val params: List<Any>?,
    val id: Long?,
    val jsonrpc: String = "2.0"
)

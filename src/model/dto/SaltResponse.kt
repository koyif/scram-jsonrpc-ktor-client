package ru.koy.model.dto

//todo: add common library for shared DTOs
data class SaltResponse(
    val salt: String,
    val i: Int,
    val rand: String
)

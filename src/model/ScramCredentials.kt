package ru.koy.model

class ScramCredentials(
    val salt: ByteArray,
    val serverKey: ByteArray,
    val storedKey: ByteArray,
    val iterations: Int
)

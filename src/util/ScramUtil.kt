package ru.koy.util

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import ove.crypto.digest.Blake2b
import ru.koy.model.ScramCredentials
import java.time.LocalTime
import java.util.*
import kotlin.experimental.xor
import kotlin.random.Random

private val logger: Logger = LoggerFactory.getLogger("ru.koy.util.ScramUtil")

class ScramUtil {
    companion object {
        private val rnd = Random(LocalTime.now().toNanoOfDay())
        private val messageDigest = Blake2b.Digest.newInstance()
        private val base64Encoder = Base64.getEncoder()
        private val base64Decoder = Base64.getDecoder()

        fun getScramCredentials(password: String, iterations: Int): ScramCredentials {
            val salt = generateSalt()
            val saltedPwd = getSalted(password, salt, iterations)
            val clientKey = getClientKey(saltedPwd)

            val scramCredentials = ScramCredentials(
                salt, getStoredKey(clientKey),
                getServerKey(saltedPwd), iterations
            )

            logger.debug("Return scram credentials: {}", scramCredentials)
            return scramCredentials
        }

        fun toBase64(byteArray: ByteArray): String {
            return base64Encoder.encodeToString(byteArray)
        }

        fun fromBase64(str: String): ByteArray {
            return base64Decoder.decode(str)
        }

        private fun getServerKey(saltedPwd: ByteArray): ByteArray {
            return hmac(saltedPwd, "Server Key".toByteArray())
        }

        private fun getStoredKey(clientKey: ByteArray): ByteArray {
            return hash(clientKey)
        }

        private fun hash(byteArray: ByteArray): ByteArray {
            return messageDigest.digest(byteArray)
        }

        private fun getClientKey(saltedPwd: ByteArray): ByteArray {
            return hmac(saltedPwd, "Client Key".toByteArray())
        }

        private fun getSalted(password: String, salt: ByteArray, iterations: Int): ByteArray {
            return hi(password.toByteArray(), salt, iterations)
        }

        private fun hi(passBytes: ByteArray, salt: ByteArray, iterations: Int): ByteArray {
            val mac = Blake2b.Mac.newInstance(passBytes)
            mac.update(salt)

            val u1 = mac.digest(ByteArray(1) { 1 })
            var prev = u1
            var result = u1

            for (i in 2..iterations) {
                val ui = hmac(passBytes, prev)
                result = xor(result, ui)
                prev = ui
            }

            return result
        }

        private fun xor(a: ByteArray, b: ByteArray): ByteArray? {
            if (a.size != b.size) {
                throw IllegalArgumentException("Argument arrays must be of the same length")
            }

            val result = ByteArray(a.size)
            for (i in a.indices) {
                result[i] = a[i].xor(b[i])
            }

            return result
        }

        private fun hmac(key: ByteArray, byteArray: ByteArray): ByteArray {
            val mac = Blake2b.Mac.newInstance(key)
            return mac.digest(byteArray)
        }


        fun generateSalt(): ByteArray {
            return rnd.nextBytes(16)
        }
    }
}

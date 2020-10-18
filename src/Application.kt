package ru.koy

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.thymeleaf.*
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver
import ru.koy.model.dto.UsernameLogin
import ru.koy.service.AuthenticationService

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused") // Referenced in application.conf
@kotlin.jvm.JvmOverloads
fun Application.module(testing: Boolean = false) {
    val authenticationService = AuthenticationService()

    routing {
        install(Thymeleaf) {
            setTemplateResolver(ClassLoaderTemplateResolver().apply {
                prefix = "templates/"
                suffix = ".html"
                characterEncoding = "utf-8"
            })
        }

        get("/registration") {
            call.respond(ThymeleafContent("registration", mapOf()))
        }

        post("/registration") {
            //todo: validation needed
            val parameters = call.receive<Parameters>()
            val data = UsernameLogin(parameters["username"] as String, parameters["password"] as String)
            val result = authenticationService.registration(data)
            call.respond(ThymeleafContent("result", mapOf("result" to result)))
        }

        get("/login") {
            call.respond(ThymeleafContent("login", mapOf()))
        }

        post("/login") {
            //todo: validation needed
            val parameters = call.receive<Parameters>()
            val data = UsernameLogin(parameters["username"] as String, parameters["password"] as String)
            val result = authenticationService.login(data)
            call.respond(ThymeleafContent("result", mapOf("result" to result)))
        }
    }

}

package com.devbuild.api.scraps

import com.devbuild.commons.auth.JwtConfig
import com.devbuild.commons.intercom.UserServiceIntercom
import com.devbuild.commons.user.User
import com.fasterxml.jackson.databind.SerializationFeature
import io.ktor.application.Application
import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.auth.Authentication
import io.ktor.auth.Principal
import io.ktor.auth.authenticate
import io.ktor.auth.authentication
import io.ktor.auth.jwt.jwt
import io.ktor.features.CORS
import io.ktor.features.ContentNegotiation
import io.ktor.http.HttpStatusCode
import io.ktor.jackson.jackson
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.response.respondText
import io.ktor.routing.*

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

data class PrincipalUser(val user: User) : Principal

val ApplicationCall.principal get() = authentication.principal<PrincipalUser>()!!

val userServiceIntercom = UserServiceIntercom("http://localhost:9090")
val scrapService = ScrapServiceImpl()

@Suppress("unused")
@kotlin.jvm.JvmOverloads
fun Application.main(testing: Boolean = false) {

    install(CORS) {
        anyHost()
    }

    install(Authentication) {
        jwt("jwt") {
            verifier(JwtConfig.verifier)
            realm = "devbuild"
            validate {
                val id = it.payload.getClaim("id").asLong()
                PrincipalUser(userServiceIntercom.findById(id))
            }
        }
    }

    install(ContentNegotiation) {
        jackson {
            enable(SerializationFeature.INDENT_OUTPUT)
        }
    }

    routing {
        get("/") {
            call.respondText("Hello, this is scraps api.")
        }
        authenticate("jwt") {
            // get scraps
            get("/api/scraps") {
                call.respond(
                    scrapService.findByAuthorId(call.principal.user.id).map { it.toDto() }
                )
            }
            // create any level scrapId
            post("/api/scraps") {
                validateRole(call, "admin")

                val scrapDto = call.receive<ScrapDto>()
                call.respond(
                    scrapService.createScrap(scrapDto.name, call.principal.user.id).toDto()
                )
            }
            // get specific scrapId
            get("/api/scraps/{id}") {
                val categoryId = call.parameters["id"]?.toLong() ?: -1L
                call.respond(
                    scrapService.findById(categoryId)!!.toDto()
                )
            }
            // update specific scrapId
            put("/api/scraps/{id}") {
                // TODO validate role or if owner!
                validateRole(call, "admin")

                val scrapDto = call.receive<ScrapDto>()
                val id = call.parameters["id"]?.toLong() ?: -1L
                val category = scrapService.findById(id)
                if (category == null) {
                    call.respond(HttpStatusCode.NotFound)
                } else {
                    scrapService.updateScrap(id, scrapDto.name, scrapDto.content)
                    call.respond(scrapService.findById(id)!!.toDto())
                }
            }
            // delete specific scrapId
            delete("/api/scraps/{id}") {
                // TODO validate role or if owner!
                validateRole(call, "admin")

                scrapService.deleteScrap(call.parameters["id"]?.toLong() ?: -1L)
            }
        }
    }
}

suspend fun validateRole(call: ApplicationCall, role: String) {
    if (call.principal == null || !call.principal.user.roles.contains(role)) {
        call.respond(HttpStatusCode.Forbidden)
    }
}

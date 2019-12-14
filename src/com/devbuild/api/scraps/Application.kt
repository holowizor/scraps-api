package com.devbuild.api.scraps

import com.devbuild.api.scraps.service.ScrapDTO
import com.devbuild.api.scraps.service.ScrapServiceImpl
import com.devbuild.api.scraps.service.Scraps
import com.devbuild.commons.auth.JwtConfig
import com.devbuild.commons.auth.UserApiClient
import com.devbuild.commons.db.Database
import com.devbuild.commons.user.UserDTO
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
import io.ktor.request.header
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.response.respondText
import io.ktor.routing.*
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

data class PrincipalUser(val user: UserDTO) : Principal
data class ScrapCreate(var name: String)

val ApplicationCall.principal get() = authentication.principal<PrincipalUser>()!!

val userApiClient = UserApiClient("http://localhost:9090")
val scrapService = ScrapServiceImpl()

@Suppress("unused")
@kotlin.jvm.JvmOverloads
fun Application.main(testing: Boolean = false) {
    Database
    transaction {
        SchemaUtils.create(Scraps)
    }

    install(CORS) {
        anyHost()
    }

    install(Authentication) {
        jwt("jwt") {
            verifier(JwtConfig.verifier)
            realm = "devbuild"
            validate {
                val authToken = this.request.header("Authorization")!!.split(" ")[1]
                val id = it.payload.getClaim("id").asInt()
                PrincipalUser(userApiClient.findById(id, authToken))
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
                    scrapService.findByAuthorId(call.principal.user.id).map { it.toDTO() }
                )
            }
            // create any level scrapId
            post("/api/scraps") {
                val scrapCreate = call.receive<ScrapCreate>()
                call.respond(
                    scrapService.createScrap(scrapCreate.name, call.principal.user.id).toDTO()
                )
            }
            // get specific scrapId
            get("/api/scraps/{id}") {
                val scrapId = call.parameters["id"]?.toInt() ?: -1
                val scrap = scrapService.findById(scrapId)
                if (scrap != null) {
                    if (scrap.authorId != call.principal.user.id) {
                        call.respond(HttpStatusCode.NotFound)
                    } else {
                        call.respond(scrap.toDTO())
                    }
                } else {
                    call.respond(HttpStatusCode.NotFound)
                }
            }
            // update specific scrapId
            put("/api/scraps/{id}") {
                val scrapDto = call.receive<ScrapDTO>()
                val id = call.parameters["id"]?.toInt() ?: -1
                val scrap = scrapService.updateScrap(id, call.principal.user.id, scrapDto.name, scrapDto.content)

                if (scrap == null) {
                    call.respond(HttpStatusCode.NotFound)
                } else {
                    call.respond(scrap!!.toDTO())
                }
            }
            // delete specific scrapId
            delete("/api/scraps/{id}") {
                scrapService.deleteScrap(call.parameters["id"]?.toInt() ?: -1, call.principal.user.id)
            }
        }
    }
}
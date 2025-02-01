package com.spendingTrack

import com.moduleUsers.routes.userRoutes
import com.spendingTrack.com.moduleSpending.routes.spendingRoutes
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.plugins.swagger.*
import io.ktor.server.plugins.cors.routing.*

fun Application.configureRouting() {
    install(CORS) {
        anyHost()
        allowHeader(HttpHeaders.ContentType)
    }
    routing {
        get("/") {
            call.respondText("Hello World!, this is an API to track personal daily spending and get information about it \n" +
                    "to see all endpoints go to /swagger")
        }
        swaggerUI(path = "swagger", swaggerFile = "openapi/documentation.yaml")
        userRoutes()
        spendingRoutes()
    }
}

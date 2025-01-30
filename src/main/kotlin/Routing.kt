package com.spendingTrack

import com.spending.track.com.moduleUsers.routes.userRoutes
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    routing {
        get("/") {
            call.respondText("Hello World!")
        }
        userRoutes()
    }
}

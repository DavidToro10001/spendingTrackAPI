package com.spendingTrack

import com.moduleUsers.routes.userRoutes
import com.spendingTrack.com.moduleSpending.routes.spendingRoutes
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    routing {
        get("/") {
            call.respondText("Hello World!")
        }
        userRoutes()
        spendingRoutes()
    }
}

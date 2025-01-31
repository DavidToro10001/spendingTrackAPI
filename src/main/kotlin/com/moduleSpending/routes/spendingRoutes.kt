package com.spendingTrack.com.moduleSpending.routes

import com.spendingTrack.com.moduleSpending.addSpend
import com.spendingTrack.com.moduleSpending.getSpends
import com.spendingTrack.com.moduleSpending.models.Spending
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.spendingRoutes() {
    authenticate("auth-jwt") {
        route("/spending") {
            // Get spending by user email
            get("/{email}") {
                val userEmail = call.parameters["email"]
                if (userEmail == null) {
                    call.respond(HttpStatusCode.BadRequest, "Invalid email")
                    return@get
                }
                // Verify that user is logged
                val principal = call.authentication.principal<JWTPrincipal>()
                val tokenEmail = principal?.payload?.getClaim("email")?.asString()

                if (tokenEmail == null || tokenEmail != userEmail) {
                    call.respond(HttpStatusCode.Forbidden, "You are not authorized to access this resource")
                    return@get
                }
                val spends = getSpends(userEmail)
                call.respond(HttpStatusCode.OK, spends)
            }

            // Save a new spend in database
            post("/add/{email}") {
                val userEmail = call.parameters["email"]
                if (userEmail == null) {
                    call.respond(HttpStatusCode.BadRequest, "Invalid email")
                    return@post
                }
                // Verify that user is logged
                val principal = call.authentication.principal<JWTPrincipal>()
                val tokenEmail = principal?.payload?.getClaim("email")?.asString()

                if (tokenEmail == null || tokenEmail != userEmail) {
                    call.respond(HttpStatusCode.Forbidden, "You are not authorized to access this resource")
                    return@post
                }
                val newSpend = call.receive<Spending>()
                if (newSpend.userEmail != userEmail) return@post call.respond(HttpStatusCode.BadRequest, "Invalid email")
                val isSuccess = addSpend(newSpend)
                if (isSuccess.not()) return@post call.respond(HttpStatusCode.InternalServerError, "Failed to add spend")
                call.respond(HttpStatusCode.Created, "Spend created correctly")
            }
        }
    }
}
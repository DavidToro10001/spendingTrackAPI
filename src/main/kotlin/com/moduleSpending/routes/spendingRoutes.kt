package com.spendingTrack.com.moduleSpending.routes

import com.spendingTrack.com.moduleSpending.addSpend
import com.spendingTrack.com.moduleSpending.deleteSpend
import com.spendingTrack.com.moduleSpending.getSpends
import com.spendingTrack.com.moduleSpending.models.Spending
import com.spendingTrack.com.moduleSpending.updateSpend
import com.spendingTrack.com.moduleSpending.getSpendInfoByDate
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

            // Delete a spend from database
            delete("/delete/{email}/{spendId}") {
                val userEmail = call.parameters["email"]
                if (userEmail == null) {
                    call.respond(HttpStatusCode.BadRequest, "Invalid email")
                    return@delete
                }
                // Verify that user is logged
                val principal = call.authentication.principal<JWTPrincipal>()
                val tokenEmail = principal?.payload?.getClaim("email")?.asString()

                if (tokenEmail == null || tokenEmail != userEmail) {
                    call.respond(HttpStatusCode.Forbidden, "You are not authorized to access this resource")
                    return@delete
                }

                val spendId = call.parameters["spendId"]
                if (spendId == null) {
                    call.respond(HttpStatusCode.BadRequest, "Invalid spendId")
                }
                val isSuccess = deleteSpend(spendId!!.toInt(), userEmail)
                if (isSuccess.not()) return@delete call.respond(HttpStatusCode.InternalServerError, "Failed to delete spend")
                call.respond(HttpStatusCode.OK, "Spend deleted correctly")
            }

            // Update spend information
            patch("/update/{email}/{spendId}") {
                val userEmail = call.parameters["email"]
                if (userEmail == null) {
                    call.respond(HttpStatusCode.BadRequest, "Invalid email")
                    return@patch
                }
                // Verify that user is logged
                val principal = call.authentication.principal<JWTPrincipal>()
                val tokenEmail = principal?.payload?.getClaim("email")?.asString()

                if (tokenEmail == null || tokenEmail != userEmail) {
                    call.respond(HttpStatusCode.Forbidden, "You are not authorized to access this resource")
                    return@patch
                }

                val spendId = call.parameters["spendId"]
                if (spendId == null) {
                    call.respond(HttpStatusCode.BadRequest, "Invalid spendId")
                }

                val updateRequest = call.receive<Spending>()
                val isSuccess = updateSpend(spendId!!.toInt(), userEmail, updateRequest)
                if (isSuccess.not()) return@patch call.respond(
                    HttpStatusCode.InternalServerError,
                    "Failed to edit spend info"
                )
                call.respond(HttpStatusCode.OK, "Spend updated correctly")
            }

            //Endpoint to get spend data in a range of dates
            get("/spendByDate/{email}/{start_date}/{end_date}") {
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
                val startDate = call.parameters["start_date"]
                val endDate = call.parameters["end_date"]
                if (startDate == null || endDate == null) {
                    call.respond(HttpStatusCode.BadRequest, "Invalid start date or end date")
                }

                val spendInfo = getSpendInfoByDate(userEmail, startDate!!, endDate!!)
                call.respond(HttpStatusCode.OK, spendInfo)
            }
        }
    }
}
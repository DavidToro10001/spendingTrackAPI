package com.moduleUsers.routes

import User
import com.spending.track.com.moduleUsers.*
import com.spendingTrack.blacklistedTokens
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.userRoutes() {
    route("/user") {
        post ("/register"){
            val newUser = call.receive<User>()
            // get user parameters
            // validate email unique and all parameters
            if (newUser.name.isBlank() || newUser.email.isBlank() || newUser.password.isNullOrBlank()) {
                call.respond(HttpStatusCode.BadRequest, "All fields are required")
                return@post
            }
            if (verifyEmailUsed(newUser.email)) return@post call.respond(HttpStatusCode.BadRequest, "email is already registered")
            // add new user to the dataBase
            val isSuccess = addUser(newUser.name, newUser.email, newUser.password!!)
            if (isSuccess.not()) return@post call.respond(HttpStatusCode.InternalServerError, "Failed to add user")
            call.respond(HttpStatusCode.Created, "User created correctly")
        }

        post("/login") {
            val loginRequest = call.receive<User>()

            // Validate email and password are not blank or null
            if (loginRequest.email.isBlank() || loginRequest.password.isNullOrBlank()) {
                call.respond(HttpStatusCode.BadRequest, "Email and password are required")
                return@post
            }

            // Authenticate
            val isSuccess = loginUser(loginRequest.email, loginRequest.password!!)
            if (!isSuccess) {
                call.respond(HttpStatusCode.Unauthorized, "email or password is incorrect")
            } else {
                // Generate JWT token
                val token = Jwt(environment.config).generateToken(loginRequest.email)
                call.respond(mapOf("token" to token))
            }
        }

        authenticate("auth-jwt") {
            get ("/get/{email}"){
                val userEmail = call.parameters["email"]

                if (userEmail == null) {
                    call.respond(HttpStatusCode.BadRequest, "Invalid email")
                    return@get
                }
                // Verify that user is logged
                val principal = call.authentication.principal<JWTPrincipal>()
                val tokenEmail = principal?.payload?.getClaim("email")?.asString()

                if (tokenEmail == null || tokenEmail != userEmail) {
                    call.respond(HttpStatusCode.Forbidden, "You are not authorized to modify this user")
                    return@get
                }

                val user = getUser(userEmail)
                call.respond(HttpStatusCode.OK, user)
            }
            patch("/update/{email}") {
                val userEmail = call.parameters["email"]

                if (userEmail == null) {
                    call.respond(HttpStatusCode.BadRequest, "Invalid email")
                    return@patch
                }
                // Verify that user is logged
                val principal = call.authentication.principal<JWTPrincipal>()
                val tokenEmail = principal?.payload?.getClaim("email")?.asString()

                if (tokenEmail == null || tokenEmail != userEmail) {
                    call.respond(HttpStatusCode.Forbidden, "You are not authorized to modify this user")
                    return@patch
                }

                val updateRequest = call.receive<User>()
                val isSuccess = updateUser(userEmail, updateRequest)
                if (isSuccess.not()) return@patch call.respond(
                    HttpStatusCode.InternalServerError,
                    "Failed to edit user info"
                )
                call.respond(HttpStatusCode.OK, "User updated correctly")
            }

            post("/logout") {
                val token = call.request.header("Authorization")?.removePrefix("Bearer ")

                if (token != null) {
                    blacklistedTokens.add(token)
                    call.respond(HttpStatusCode.OK, "You have been logged out successfully.")
                } else {
                    call.respond(HttpStatusCode.BadRequest, "Missing or invalid token.")
                }
            }

        }
    }
}
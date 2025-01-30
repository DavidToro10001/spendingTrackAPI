package com.spendingTrack

import com.spending.track.com.moduleUsers.Jwt
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*

val blacklistedTokens = mutableSetOf<String>()

fun Application.configureSecurity() {
    val jwtConfig = Jwt(environment.config)

    install(Authentication) {
        jwt("auth-jwt") {
            realm = jwtConfig.realm
            verifier(jwtConfig.configureKtorAuth())
            validate { credential ->
                val rawToken = this.request.header("Authorization")?.removePrefix("Bearer ")
                // Verify if token is in blacklist
                if (rawToken != null && blacklistedTokens.contains(rawToken)) {
                    null
                } else if (credential.payload.getClaim("email").asString() != null) {
                    // Validate email claim
                    JWTPrincipal(credential.payload)
                } else {
                    null
                }
            }
        }

    }
}

package com.spending.track.com.moduleUsers

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.server.config.*
import java.util.*

class Jwt(config: ApplicationConfig) {

    private val secret = config.property("ktor.jwt.secret").getString()
    private val issuer = config.property("ktor.jwt.issuer").getString()
    private val audience = config.property("ktor.jwt.audience").getString()
    private val validityInMs = config.property("ktor.jwt.validityInMs").getString().toLong()
    val realm = config.property("ktor.jwt.realm").getString()

    private val algorithm = Algorithm.HMAC256(secret)

    /**
     * Generate token fot the user
     */
    fun generateToken(userEmail: String): String {
        return JWT.create()
            .withIssuer(issuer)
            .withAudience(audience)
            .withClaim("email", userEmail)
            .withExpiresAt(getExpiration())
            .sign(algorithm)
    }

    /**
     * validate token using config
     */
    fun configureKtorAuth(): JWTVerifier {
        return JWT
            .require(algorithm)
            .withIssuer(issuer)
            .withAudience(audience)
            .build()
    }

    /**
     * get Token expiration
     */
    private fun getExpiration() = Date(System.currentTimeMillis() + validityInMs)
}
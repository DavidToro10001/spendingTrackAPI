package com.spending.track.com.moduleUsers

import User
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64

fun encodePassword(password: String, salt: ByteArray): String {
    val digest = MessageDigest.getInstance("SHA-256")
    digest.update(salt)
    val hashedBytes = digest.digest(password.toByteArray())
    return Base64.getEncoder().encodeToString(hashedBytes)
}

fun generateSalt(): ByteArray {
    val random = SecureRandom()
    val salt = ByteArray(16)
    random.nextBytes(salt)
    return salt
}

fun verifyPassword(rawPassword: String, hashedPassword: String, salt: ByteArray): Boolean {
    val encodedPassword = encodePassword(rawPassword, salt)
    return encodedPassword == hashedPassword
}

fun verifyEmailUsed(email: String): Boolean {
    return transaction {
        Users.select { Users.email eq email }.count() > 0
    }
}

fun addUser(name: String, email: String, password: String): Boolean {
    return try {
        val salt = generateSalt()
        val hashedPassword = encodePassword(password, salt)
        transaction {
            Users.insert {
                it[Users.name] = name
                it[Users.email] = email
                it[Users.password] = "$hashedPassword:${Base64.getEncoder().encodeToString(salt)}"
            }
        }
        true
    } catch (e: Exception) {
        false
    }
}

fun getUser(email: String): User {
    return transaction {
        Users.select { Users.email eq email }.map {
            User(
                name = it[Users.name],
                email = it[Users.email],
                isActive = it[Users.isActive]
            )
        }.first()
    }
}

fun loginUser(email: String, password: String): Boolean {
    return transaction {
        val userRow = Users.select { Users.email eq email }.firstOrNull()

        // if email not exist, login fails
        if (userRow == null) {
            return@transaction false
        }

        // get stored password
        val storedPassword = userRow[Users.password]
        val (storedHash, storedSalt) = storedPassword.split(":")
        val salt = Base64.getDecoder().decode(storedSalt)

        // Validate password
        val isPasswordValid = verifyPassword(password, storedHash, salt)
        return@transaction isPasswordValid
    }
}

fun updateUser(email: String, parameters: User): Boolean {
    return try {
        transaction {
            // Verify that email is a real user registered
            val userRow = Users.select { Users.email eq email }.firstOrNull()

            if (userRow == null) {
                return@transaction false
            }

            // Update valid fields
            Users.update({ Users.email eq email }) { updateQuery ->
                if (parameters.name.isNotBlank()) updateQuery[name] = parameters.name
                if (parameters.email.isNotBlank()) updateQuery[Users.email] = parameters.email
                if (parameters.isActive != null) updateQuery[isActive] = parameters.isActive!!
                if (parameters.password != null) {
                    val salt = generateSalt()
                    val hashedPassword = encodePassword(parameters.password!!, salt)
                    updateQuery[password] = "$hashedPassword:${Base64.getEncoder().encodeToString(salt)}"
                }
            }
        }
        true
    } catch (e: Exception) {
        false
    }
}

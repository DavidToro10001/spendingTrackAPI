package com.spendingTrack

import Users
import com.spendingTrack.com.moduleSpending.models.DefaultSpendCategories
import com.spendingTrack.com.moduleSpending.models.Spendings
import com.spendingTrack.com.moduleSpending.models.SpendCategories
import io.ktor.server.application.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.nio.file.Files
import java.nio.file.Paths

fun Application.configureDatabase() {
    // relative route
    val dbPath = "db/spendingDb.sqlite"

    // Creates directory if not exists
    val dbDirectory = Paths.get("db")
    if (!Files.exists(dbDirectory)) {
        Files.createDirectories(dbDirectory)
    }

    // Connect to database
    Database.connect("jdbc:sqlite:$dbPath", driver = "org.sqlite.JDBC", user = "root",
        password = "password"
    )

    // tables creation
    transaction {
        SchemaUtils.create(Users)
        SchemaUtils.create(SpendCategories)
        SchemaUtils.create(Spendings)
        // Insert spent categories
        DefaultSpendCategories.entries.forEach { category ->
            if (SpendCategories.select { SpendCategories.displayName eq category.displayName }.empty()) {
                SpendCategories.insert {
                    it[displayName] = category.displayName
                }
            }
        }

    }
}
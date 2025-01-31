package com.spendingTrack.com.moduleSpending

import com.spendingTrack.com.moduleSpending.models.DefaultSpendCategories
import com.spendingTrack.com.moduleSpending.models.Spending
import com.spendingTrack.com.moduleSpending.models.Spendings
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDate
import java.time.format.DateTimeFormatter

fun getSpends(email: String): List<Spending> {
    return transaction {
        Spendings.select {Spendings.user eq email}.map {
            Spending(
                name = it[Spendings.name],
                cost = it[Spendings.cost],
                date = it[Spendings.date].toString(),
                category = it[Spendings.category],
                userEmail = email
            )
        }
    }
}

fun addSpend(spending: Spending): Boolean {
    if (!DefaultSpendCategories.entries.any { it.displayName == spending.category }) {
        return false
    }
    return try {
        transaction {
            Spendings.insert {
                it[Spendings.name] = spending.name;
                it[Spendings.cost] = spending.cost;
                it[Spendings.date] = LocalDate.parse(spending.date, DateTimeFormatter.ISO_DATE)
                it[Spendings.category] = spending.category;
                it[Spendings.user] = spending.userEmail;
            }
        }
        true
    } catch (e: Exception) {
        false
    }
}
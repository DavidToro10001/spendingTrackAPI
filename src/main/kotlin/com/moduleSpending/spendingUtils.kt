package com.spendingTrack.com.moduleSpending

import com.spendingTrack.com.moduleSpending.models.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDate
import java.time.format.DateTimeFormatter

fun getSpends(email: String): List<SpendingGetResponse> {
    return transaction {
        Spendings.select { Spendings.user eq email }.map {
            SpendingGetResponse(
                id = it[Spendings.id].value,
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

fun deleteSpend(spendingId: Int, email: String): Boolean {
    return transaction {
        val deletedRow = Spendings.deleteWhere {(Spendings.id eq spendingId) and (Spendings.user eq email)}
        return@transaction (deletedRow == 1)
    }
}

fun updateSpend(spendingId: Int, email: String, parameters: Spending): Boolean {
    // If category is being changed, validate
    if (!DefaultSpendCategories.entries.any { it.displayName == parameters.category } && parameters.category != "") {
        return false
    }
    return transaction {
        // Verify that the user email is the owner of the spend
        val spendingRow =
            Spendings.select { (Spendings.id eq spendingId) and (Spendings.user eq email) }.firstOrNull()

        if (spendingRow == null) {
            return@transaction false
        }

        // Update valid fields
        val result = Spendings.update({ Spendings.id eq spendingId }) { updateQuery ->
            if (parameters.name.isNotBlank()) updateQuery[name] = parameters.name
            if (!parameters.cost.isNaN()) updateQuery[cost] = parameters.cost
            if (parameters.date.isNotBlank()) updateQuery[date] = LocalDate.parse(parameters.date, DateTimeFormatter.ISO_DATE)
            if (parameters.category.isNotBlank()) updateQuery[category] = parameters.category
        }
        // Only one row should be affected
        return@transaction result == 1
    }
}

fun getSpendInfoByDate(email: String, startDate: String, endDate: String): List<SpendInfoByCategory> {
    val startDateParsed = LocalDate.parse(startDate, DateTimeFormatter.ISO_DATE)
    val endDateParsed = LocalDate.parse(endDate, DateTimeFormatter.ISO_DATE)
    return transaction {
        val query = Spendings
            .slice(Spendings.category, Spendings.cost.sum(), Spendings.cost.avg())  // Select just category costs total and average
            .select {
                (Spendings.user eq email) and
                        (Spendings.date greaterEq startDateParsed) and
                        (Spendings.date lessEq endDateParsed)
            }
            .groupBy(Spendings.category)

        val totalSpending = query.sumOf { it[Spendings.cost.sum()] ?: 0.0 }

        query.map {
            val totalSpendingCategory = it[Spendings.cost.sum()] ?: 0.0
            val mean = it[Spendings.cost.avg()] ?: 0.0
            SpendInfoByCategory(
                category = it[Spendings.category],
                spendInfo = CostData(
                    totalCost = totalSpendingCategory,
                    mean = mean.toDouble(),
                    percentage = if (totalSpending > 0) (totalSpendingCategory / totalSpending) * 100 else 0.0
                )
            )
        }
    }
}
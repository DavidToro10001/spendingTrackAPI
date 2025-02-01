package com.spendingTrack.com.moduleSpending.models

import Users
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.date

@Serializable
data class Spending(val userEmail: String = "", val name: String = "", val cost: Double = Double.NaN, val date: String = "", val category: String = "")

@Serializable
data class SpendingGetResponse(
    val id: Int,
    val userEmail: String,
    val name: String = "",
    val cost: Double,
    val date: String,
    val category: String
)

object Spendings : IntIdTable() {
    val user = reference("user", Users.email) // Foreign key for associated user
    val category = reference("category", SpendCategories.displayName) // Foreign key for category type
    val name = varchar("name", 255)
    val cost = double("cost")
    val date = date("date")
}
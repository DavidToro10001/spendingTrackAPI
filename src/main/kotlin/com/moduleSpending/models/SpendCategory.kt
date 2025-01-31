package com.spendingTrack.com.moduleSpending.models

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.dao.id.IntIdTable

@Serializable
enum class DefaultSpendCategories(val displayName: String) {
    HOUSING("Housing"),
    FOOD("Food"),
    TRANSPORT("Transport"),
    PERSONAL("Personal"),
    UTILITIES("Utilities"),
    SAVINGS("Savings"),
    INVESTMENTS("Investments"),
    ENTERTAINMENT("Entertainment"),
    OTHER("Other"),
}

object SpendCategories : IntIdTable() {
    val displayName = varchar("displayName", 255).uniqueIndex()
}
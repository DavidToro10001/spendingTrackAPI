package com.spendingTrack.com.moduleSpending.models

import kotlinx.serialization.Serializable

@Serializable
data class SpendInfoByCategory(val category: String, val spendInfo: CostData)
package com.spendingTrack.com.moduleSpending.models

import kotlinx.serialization.Serializable

@Serializable
data class CostData(val totalCost: Double, val mean: Double, val percentage: Double)
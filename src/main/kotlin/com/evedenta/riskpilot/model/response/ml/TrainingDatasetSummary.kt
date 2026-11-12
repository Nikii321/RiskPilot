package com.evedenta.riskpilot.model.response.ml

data class TrainingDatasetSummary(val id: String,val sourceName: String,val fingerprint: String,val rows: Int,val uploadedAt: String)

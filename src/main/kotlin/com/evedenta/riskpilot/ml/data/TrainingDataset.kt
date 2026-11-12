package com.evedenta.riskpilot.ml.data

data class TrainingDataset(val id: String,val sourceName: String,val provenance: String,val csv: String,
    val fingerprint: String,val rows: Int,val uploadedAt: String)

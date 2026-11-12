package com.evedenta.riskpilot.ml.model



data class ModelSnapshot(val id: String, val createdAt: String, val sourceName: String,
    val report: ModelReport, val datasetCsv: String, val trainingFingerprint: String? = null)

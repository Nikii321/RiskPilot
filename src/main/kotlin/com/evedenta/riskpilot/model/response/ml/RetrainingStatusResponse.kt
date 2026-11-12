package com.evedenta.riskpilot.model.response.ml

import com.evedenta.riskpilot.ml.model.TrainingRun

data class RetrainingStatusResponse(val enabled: Boolean,val cron: String,val zone: String,val nextRunAt: String?,
    val sourcePath: String,val queuedDataset: TrainingDatasetSummary?,val runs: List<TrainingRun>,
    val minValidationAuc: Double,val maxBrierRegression: Double)

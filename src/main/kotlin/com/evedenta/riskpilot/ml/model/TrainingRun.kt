package com.evedenta.riskpilot.ml.model

data class TrainingRun(val id: String,val startedAt: String,val finishedAt: String?,val status: TrainingRunStatus,
    val trigger: String,val sourceName: String?,val fingerprint: String?,val modelId: String?,val message: String)

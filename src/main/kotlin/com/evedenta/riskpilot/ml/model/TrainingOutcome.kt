package com.evedenta.riskpilot.ml.model

data class TrainingOutcome(val status: TrainingRunStatus,val modelId: String,val message: String)

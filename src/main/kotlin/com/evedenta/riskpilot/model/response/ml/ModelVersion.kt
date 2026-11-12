package com.evedenta.riskpilot.model.response.ml

import com.evedenta.riskpilot.ml.model.ModelReport

data class ModelVersion(val id: String, val createdAt: String, val sourceName: String, val rows: Int, val report: ModelReport)

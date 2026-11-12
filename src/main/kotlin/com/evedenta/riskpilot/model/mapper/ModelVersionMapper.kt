package com.evedenta.riskpilot.model.mapper

import com.evedenta.riskpilot.ml.model.ModelSnapshot
import com.evedenta.riskpilot.model.response.ml.ModelVersion

object ModelVersionMapper {
    fun from(snapshot: ModelSnapshot) = with(snapshot) {
        ModelVersion(id,createdAt,sourceName,report.trainCount+report.validationCount+report.testCount,report)
    }
}

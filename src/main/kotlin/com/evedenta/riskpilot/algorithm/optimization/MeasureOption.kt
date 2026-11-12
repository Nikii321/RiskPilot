package com.evedenta.riskpilot.algorithm.optimization

import com.evedenta.riskpilot.domain.mitigation.MitigationMeasure

data class MeasureOption(val measure: MitigationMeasure, val gain: Double)

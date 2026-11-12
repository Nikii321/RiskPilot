package com.evedenta.riskpilot.ml.model

import kotlin.math.*

data class ModelMetrics(val count: Int, val positives: Int, val accuracy: Double, val precision: Double,
    val recall: Double, val rocAuc: Double, val brier: Double, val logLoss: Double, val baselineBrier: Double,
    val truePositive: Int, val falsePositive: Int, val trueNegative: Int, val falseNegative: Int,
    val calibration: List<CalibrationBin>)

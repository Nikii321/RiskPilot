package com.evedenta.riskpilot.ml.model

import kotlin.math.*

data class CalibrationBin(val lower: Double, val upper: Double, val count: Int, val predicted: Double?, val observed: Double?)

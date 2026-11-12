package com.evedenta.riskpilot.ml.model

import kotlin.math.*

data class ModelReport(val name: String, val provenance: String, val seed: Long, val trainCount: Int,
    val validationCount: Int, val testCount: Int, val projectSplit: String, val threshold: Double,
    val features: List<String>, val coefficients: List<Double>, val intercept: Double,
    val means: List<Double>, val scales: List<Double>, val metrics: ModelMetrics,
    val engine: String = "Kotlin gradient descent (legacy)",
    val optimizer: String = "Full-batch gradient descent",
    val maxIterations: Int = 1800,
    val regularization: Double = 0.01,
    val validationMetrics: ModelMetrics? = null)

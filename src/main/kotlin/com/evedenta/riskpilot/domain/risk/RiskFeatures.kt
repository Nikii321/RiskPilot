package com.evedenta.riskpilot.domain.risk



data class RiskFeatures(
    val complexity: Double = 0.5, val experience: Double = 0.5,
    val workload: Double = 0.5, val dependencies: Int = 3,
    val volatility: Double = 0.5, val reliability: Double = 0.5
) {
    fun vector() = doubleArrayOf(complexity, experience, workload, dependencies / 10.0, volatility, reliability)
    fun validate() {
        require(vector().all { it.isFinite() && it in 0.0..1.0 }) { "Признаки должны быть в [0,1], зависимости — от 0 до 10" }
        require(dependencies in 0..10) { "Число зависимостей: 0–10" }
    }
}

package com.evedenta.riskpilot.model.request.analysis

import com.evedenta.riskpilot.config.DefaultAnalysisConfig
import com.evedenta.riskpilot.domain.scenario.Scenario

data class AnalysisRequest(
    val budget: Int = 300, val capacityDays: Int = 25, val dayCost: Double = 25.0,
    val matrix: List<List<Double>> = DefaultAnalysisConfig.defaultMatrix(),
    val scenarios: List<Scenario> = DefaultAnalysisConfig.defaultScenarios()
) {
    fun validate() {
        require(budget in 0..100000 && capacityDays in 0..365) { "Бюджет: 0–100000 тыс. ₽, ресурс: 0–365 чел.-дней" }
        require(dayCost.isFinite() && dayCost in 0.0..1000.0) { "Стоимость дня: 0–1000 тыс. ₽" }
        require(scenarios.size == 3) { "Необходимо ровно три сценария" }
        require(scenarios.all { s -> s.name.isNotBlank() && s.name.length <= 60 &&
            s.probability.isFinite() && s.probability in 0.0..1.0 &&
            s.probabilityFactor.isFinite() && s.probabilityFactor in 0.1..3.0 &&
            s.lossFactor.isFinite() && s.lossFactor in 0.1..3.0 }) { "Некорректные параметры сценария" }
        require(kotlin.math.abs(scenarios.sumOf { it.probability } - 1.0) < 1e-8) { "Сумма вероятностей сценариев должна равняться 1" }
        require(scenarios.map { it.name }.distinct().size == 3) { "Названия сценариев должны различаться" }
    }
}

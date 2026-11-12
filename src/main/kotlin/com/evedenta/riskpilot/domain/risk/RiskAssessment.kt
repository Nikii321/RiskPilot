package com.evedenta.riskpilot.domain.risk



data class RiskAssessment(
    val title: String, val category: RiskCategory, val owner: String,
    val loss: Double, val delayDays: Int, val urgency: Double, val features: RiskFeatures,
    val version: Int = 0
) {
    fun validate() {
        require(title.isNotBlank() && title.length <= 160) { "Название риска: 1–160 символов" }
        require(owner.isNotBlank() && owner.length <= 100) { "Ответственный: 1–100 символов" }
        require(loss.isFinite() && loss in 0.0..100000.0) { "Прямой ущерб: 0–100000 тыс. ₽" }
        require(delayDays in 0..365 && urgency.isFinite() && urgency in 1.0..5.0) { "Задержка: 0–365 дней, срочность: 1–5" }
        require(version >= 0) { "Версия должна быть неотрицательной" }
        features.validate()
    }
}

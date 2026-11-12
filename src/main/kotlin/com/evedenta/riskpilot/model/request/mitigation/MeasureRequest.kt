package com.evedenta.riskpilot.model.request.mitigation



data class MeasureRequest(val title: String,val cost: Int,val days: Int,val reduction: Double) {
    fun validate() {
        require(title.isNotBlank() && title.length<=160) { "Название меры: 1–160 символов" }
        require(cost in 0..100000 && days in 0..365) { "Стоимость: 0–100000 тыс. ₽; трудозатраты: 0–365 дней" }
        require(reduction.isFinite() && reduction in 0.0..1.0) { "Снижение ущерба должно быть в [0,1]" }
    }
}

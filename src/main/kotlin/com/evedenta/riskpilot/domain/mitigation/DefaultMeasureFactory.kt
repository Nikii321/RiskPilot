package com.evedenta.riskpilot.domain.mitigation

import com.evedenta.riskpilot.domain.risk.*
import kotlin.math.max

object DefaultMeasureFactory {
    fun forRisk(risk: Risk): List<MitigationMeasure> {
        val cost = max(15, (risk.input.loss*0.055).toInt())
        val names = when(risk.input.category) {
            RiskCategory.TECHNICAL -> "Технический аудит и репетиция" to "Резервирование и поэтапное внедрение"
            RiskCategory.PEOPLE -> "Передача знаний и наставничество" to "Подготовка резервного специалиста"
            RiskCategory.SUPPLIER -> "Контрольные поставки и SLA" to "Подключение резервного поставщика"
            RiskCategory.SECURITY -> "Аудит безопасности" to "Аудит и устранение уязвимостей"
            RiskCategory.REQUIREMENTS -> "Фиксация базовых требований" to "Управление изменениями и прототипирование"
            RiskCategory.SCHEDULE -> "Регламент согласования" to "Резервирование ресурсов и сроков"
        }
        return listOf(MitigationMeasure("${risk.id}:basic",risk.id,names.first,cost,3,.35),
            MitigationMeasure("${risk.id}:extended",risk.id,names.second,cost*2,6,.60))
    }
}

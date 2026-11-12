package com.evedenta.riskpilot.domain.risk



enum class RiskCategory(val label: String) {
    TECHNICAL("Технический"), PEOPLE("Кадровый"), SCHEDULE("Сроки"),
    SUPPLIER("Поставщики"), REQUIREMENTS("Требования"), SECURITY("Безопасность")
}

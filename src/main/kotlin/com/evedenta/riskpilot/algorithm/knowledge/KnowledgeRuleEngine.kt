package com.evedenta.riskpilot.algorithm.knowledge

import com.evedenta.riskpilot.domain.knowledge.KnowledgeRule
import com.evedenta.riskpilot.domain.knowledge.RuleHit
import com.evedenta.riskpilot.domain.risk.Risk
import com.evedenta.riskpilot.domain.risk.RiskCategory

object KnowledgeRuleEngine {
    val rules = listOf(
        KnowledgeRule("K1","p ≥ 0.60 и полный ущерб ≥ 800 тыс. ₽","Критический риск: еженедельный контроль руководителем"),
        KnowledgeRule("K2","загрузка ≥ 0.80 и опыт ≤ 0.50","Назначить наставника и снизить перегрузку"),
        KnowledgeRule("K3","категория Поставщики и надёжность < 0.50","Подготовить альтернативного поставщика"),
        KnowledgeRule("K4","категория Требования и изменчивость ≥ 0.70","Ввести контроль изменений и согласовать базовый объём"),
        KnowledgeRule("K5","категория Безопасность и прямой ущерб ≥ 1000 тыс. ₽","Провести проверку безопасности до релиза"),
        KnowledgeRule("K6","задержка ≥ 14 дней и срочность ≥ 4","Эскалировать владельцу критического пути")
    )
    fun infer(risk: Risk, p: Double, dayCost: Double): List<RuleHit> {
        val i=risk.input; val f=i.features
        val facts=listOf(
            listOf("p=$p","полный ущерб=${i.loss+i.delayDays*dayCost}"),
            listOf("загрузка=${f.workload}","опыт=${f.experience}"),
            listOf("категория=${i.category.label}","надёжность=${f.reliability}"),
            listOf("категория=${i.category.label}","изменчивость=${f.volatility}"),
            listOf("категория=${i.category.label}","прямой ущерб=${i.loss}"),
            listOf("задержка=${i.delayDays}","срочность=${i.urgency}")
        )
        val matches=listOf(p>=.6 && i.loss+i.delayDays*dayCost>=800,
            f.workload>=.8 && f.experience<=.5,i.category==RiskCategory.SUPPLIER && f.reliability<.5,
            i.category==RiskCategory.REQUIREMENTS && f.volatility>=.7,i.category==RiskCategory.SECURITY && i.loss>=1000,
            i.delayDays>=14 && i.urgency>=4)
        return rules.indices.filter { matches[it] }.map { RuleHit(rules[it].id,facts[it],rules[it].conclusion) }
    }
}

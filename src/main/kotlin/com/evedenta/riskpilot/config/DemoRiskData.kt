package com.evedenta.riskpilot.config

import com.evedenta.riskpilot.domain.risk.*

object DemoRiskData {
    fun risks() = listOf(
        RiskAssessment("Срыв поставки интеграционного API",RiskCategory.SUPPLIER,"Анна Смирнова",900.0,18,5.0,RiskFeatures(.8,.6,.85,8,.6,.25)),
        RiskAssessment("Уход ведущего разработчика",RiskCategory.PEOPLE,"Иван Петров",600.0,15,4.0,RiskFeatures(.7,.45,.95,5,.5,.8)),
        RiskAssessment("Рост объёма требований",RiskCategory.REQUIREMENTS,"Мария Волкова",750.0,20,5.0,RiskFeatures(.7,.65,.8,7,.95,.8)),
        RiskAssessment("Уязвимость модуля авторизации",RiskCategory.SECURITY,"Олег Кузнецов",1500.0,8,5.0,RiskFeatures(.9,.7,.6,4,.4,.7)),
        RiskAssessment("Сбой миграции данных",RiskCategory.TECHNICAL,"Елена Соколова",1100.0,12,4.0,RiskFeatures(.95,.5,.75,8,.6,.6)),
        RiskAssessment("Задержка согласования дизайна",RiskCategory.SCHEDULE,"Дмитрий Орлов",220.0,10,3.0,RiskFeatures(.4,.8,.65,4,.7,.85)),
        RiskAssessment("Недоступность тестового стенда",RiskCategory.TECHNICAL,"Павел Морозов",350.0,7,3.0,RiskFeatures(.55,.75,.6,6,.4,.6)),
        RiskAssessment("Недостаток компетенций команды",RiskCategory.PEOPLE,"Иван Петров",500.0,14,4.0,RiskFeatures(.8,.2,.85,5,.65,.8))
    )
}

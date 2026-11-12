package com.evedenta.riskpilot.ml.data

object ProjectDatasetSplitter {
    fun split(rows: List<HistoricalObservation>): DatasetSplit {
        require(rows.size in 90..5000) { "Для обучения необходимо 90–5000 наблюдений" }
        val projects=rows.map { it.projectId }.distinct().sorted().shuffled(kotlin.random.Random(42))
        require(projects.size>=30) { "Необходимо минимум 30 различных проектов" }
        val trainIds=projects.take((projects.size*.7).toInt()).toSet()
        val validationIds=projects.drop(trainIds.size).take((projects.size*.15).toInt()).toSet()
        val result=DatasetSplit(rows.filter { it.projectId in trainIds },rows.filter { it.projectId in validationIds },
            rows.filter { it.projectId !in trainIds && it.projectId !in validationIds },trainIds.size,validationIds.size,projects.size-trainIds.size-validationIds.size)
        for((name,part) in listOf("train" to result.train,"validation" to result.validation,"test" to result.test)) {
            require(part.map { it.occurred }.toSet()==setOf(0,1)) { "В выборке $name должны присутствовать оба класса. Добавьте проекты и исходы" }
            part.forEach { it.features.validate() }
        }
        return result
    }
}

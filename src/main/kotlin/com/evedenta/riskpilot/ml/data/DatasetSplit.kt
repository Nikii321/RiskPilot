package com.evedenta.riskpilot.ml.data

data class DatasetSplit(val train: List<HistoricalObservation>,val validation: List<HistoricalObservation>,
    val test: List<HistoricalObservation>,val trainProjects: Int,val validationProjects: Int,val testProjects: Int)

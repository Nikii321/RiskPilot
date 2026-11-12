package com.evedenta.riskpilot.ml.data

import com.evedenta.riskpilot.domain.risk.RiskFeatures

object HistoryCsvCodec {
    const val HEADER="project_id,complexity,experience,workload,dependencies,volatility,reliability,occurred"
    fun parse(csv: String): List<HistoricalObservation> {
        require(csv.length<=1_000_000) { "CSV не должен превышать 1 МБ" }
        val lines=csv.removePrefix("\uFEFF").lineSequence().map { it.trim() }.filter { it.isNotEmpty() }.toList()
        require(lines.firstOrNull()==HEADER) { "Заголовок CSV должен быть: $HEADER" }
        require(lines.size in 91..5001) { "CSV должен содержать 90–5000 записей" }
        return lines.drop(1).mapIndexed { index,line ->
            try {
                val v=line.split(','); require(v.size==8)
                val project=v[0].toInt(); val target=v[7].toInt()
                require(project>0 && target in 0..1)
                val f=RiskFeatures(v[1].toDouble(),v[2].toDouble(),v[3].toDouble(),v[4].toInt(),v[5].toDouble(),v[6].toDouble())
                f.validate(); HistoricalObservation(project,f,target)
            } catch(_: IllegalArgumentException) { throw IllegalArgumentException("Некорректная строка CSV ${index+2}: проверьте восемь числовых полей, диапазоны и класс 0/1") }
        }
    }
    fun write(rows: List<HistoricalObservation>): String = HEADER+"\n"+rows.joinToString("\n") { r -> with(r.features) {
        "${r.projectId},$complexity,$experience,$workload,$dependencies,$volatility,$reliability,${r.occurred}"
    } }
}

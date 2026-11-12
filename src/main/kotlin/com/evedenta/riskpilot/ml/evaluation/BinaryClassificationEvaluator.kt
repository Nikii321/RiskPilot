package com.evedenta.riskpilot.ml.evaluation

import com.evedenta.riskpilot.ml.data.HistoricalObservation
import com.evedenta.riskpilot.ml.model.CalibrationBin
import com.evedenta.riskpilot.ml.model.ModelMetrics
import kotlin.math.ln
import kotlin.math.min
import kotlin.math.pow

object BinaryClassificationEvaluator {
    fun evaluate(rows: List<HistoricalObservation>,probabilities: List<Double>,threshold: Double,baseline: Double): ModelMetrics {
        require(rows.size==probabilities.size && rows.isNotEmpty())
        val p=probabilities.map { it.coerceIn(1e-12,1-1e-12) }
        val positives=rows.indices.filter { rows[it].occurred==1 }.map { p[it] }
        val negatives=rows.indices.filter { rows[it].occurred==0 }.map { p[it] }
        require(positives.isNotEmpty() && negatives.isNotEmpty())
        val tp=rows.indices.count { p[it]>=threshold && rows[it].occurred==1 }
        val fp=rows.indices.count { p[it]>=threshold && rows[it].occurred==0 }
        val tn=rows.indices.count { p[it]<threshold && rows[it].occurred==0 }
        val fn=rows.size-tp-fp-tn
        val auc=positives.sumOf { positive -> negatives.sumOf { negative -> if(positive>negative) 1.0 else if(positive==negative) .5 else 0.0 } }/(positives.size*negatives.size)
        return ModelMetrics(rows.size,positives.size,(tp+tn).toDouble()/rows.size,tp.toDouble()/(tp+fp).coerceAtLeast(1),
            tp.toDouble()/(tp+fn).coerceAtLeast(1),auc,rows.indices.sumOf { (p[it]-rows[it].occurred).pow(2) }/rows.size,
            -rows.indices.sumOf { rows[it].occurred*ln(p[it])+(1-rows[it].occurred)*ln(1-p[it]) }/rows.size,
            rows.sumOf { (baseline-it.occurred).pow(2) }/rows.size,tp,fp,tn,fn,(0..4).map { bin ->
                val indices=rows.indices.filter { min(4,(p[it]*5).toInt())==bin }
                CalibrationBin(bin/5.0,(bin+1)/5.0,indices.size,if(indices.isEmpty()) null else indices.map { p[it] }.average(),
                    if(indices.isEmpty()) null else indices.map { rows[it].occurred }.average())
            })
    }
}

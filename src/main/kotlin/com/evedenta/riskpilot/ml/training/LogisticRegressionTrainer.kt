package com.evedenta.riskpilot.ml.training

import com.evedenta.riskpilot.domain.risk.RiskFeatures
import com.evedenta.riskpilot.ml.data.HistoricalObservation
import com.evedenta.riskpilot.ml.data.ProjectDatasetSplitter
import com.evedenta.riskpilot.ml.evaluation.BinaryClassificationEvaluator
import com.evedenta.riskpilot.ml.model.ModelReport
import smile.classification.LogisticRegression
import kotlin.math.exp
import kotlin.math.pow
import kotlin.math.sqrt

/** Data preparation and evaluation remain explicit; Smile fits the numerical model. */
class LogisticRegressionTrainer {
    companion object {
        const val ENGINE="Smile 4.4.2"
        const val FINGERPRINT="smile-4.4.2:logistic-lbfgs:lambda-mean-0.01:tol-1e-6:max-500:split-42:v2"
    }
    private val names=listOf("Сложность","Опыт команды","Загрузка","Зависимости / 10","Изменчивость требований","Надёжность поставщика")
    fun train(observations: List<HistoricalObservation>,provenance: String): ModelReport {
        val split=ProjectDatasetSplitter.split(observations)
        val train=split.train
        val means=DoubleArray(6) { j -> train.map { it.features.vector()[j] }.average() }
        val scales=DoubleArray(6) { j -> sqrt(train.sumOf { (it.features.vector()[j]-means[j]).pow(2) }/train.size).coerceAtLeast(1e-9) }
        fun transform(f: RiskFeatures) = f.vector().mapIndexed { j,value -> (value-means[j])/scales[j] }.toDoubleArray()
        // Smile minimizes summed log loss. Multiply lambda by N to match mean-log-loss L2=0.01.
        val fitted=LogisticRegression.binomial(train.map { transform(it.features) }.toTypedArray(),
            train.map { it.occurred }.toIntArray(),LogisticRegression.Options(.01*train.size,1e-6,500))
        val coefficients=fitted.coefficients()
        val weights=coefficients.take(6)
        val intercept=coefficients.last()
        fun predict(f: RiskFeatures): Double {
            val x=transform(f)
            val score=intercept+weights.indices.sumOf { weights[it]*x[it] }
            return 1.0/(1.0+exp(-score.coerceIn(-35.0,35.0)))
        }
        val validation=split.validation
        val validationProbabilities=validation.map { predict(it.features) }
        val threshold=(20..80).map { it/100.0 }.maxBy { t ->
            val positives=validation.indices.filter { validation[it].occurred==1 }
            val negatives=validation.indices.filter { validation[it].occurred==0 }
            (positives.count { validationProbabilities[it]>=t }.toDouble()/positives.size+
                negatives.count { validationProbabilities[it]<t }.toDouble()/negatives.size)/2
        }
        val prevalence=train.map { it.occurred }.average()
        return ModelReport("Логистическая регрессия L2",provenance,42,train.size,validation.size,split.test.size,
            "${split.trainProjects} / ${split.validationProjects} / ${split.testProjects} проектов; группы не пересекаются",
            threshold,names,weights,intercept,means.toList(),scales.toList(),
            BinaryClassificationEvaluator.evaluate(split.test,split.test.map { predict(it.features) },threshold,prevalence),
            ENGINE,"L-BFGS",500,.01,
            BinaryClassificationEvaluator.evaluate(validation,validationProbabilities,threshold,prevalence))
    }
}

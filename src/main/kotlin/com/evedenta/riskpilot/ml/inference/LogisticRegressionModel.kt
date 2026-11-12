package com.evedenta.riskpilot.ml.inference

import com.evedenta.riskpilot.domain.risk.RiskFeatures
import com.evedenta.riskpilot.ml.model.ModelReport
import kotlin.math.exp

/** Immutable fitted model. report contains every parameter required for inference. */
class LogisticRegressionModel(val report: ModelReport) {
    fun predict(features: RiskFeatures): Double {
        features.validate()
        val score=report.intercept+explain(features).values.sum()
        return 1.0/(1.0+exp(-score.coerceIn(-35.0,35.0)))
    }
    fun explain(features: RiskFeatures): Map<String,Double> {
        val vector=features.vector()
        return report.features.indices.associate { j -> report.features[j] to report.coefficients[j]*(vector[j]-report.means[j])/report.scales[j] }
    }
}

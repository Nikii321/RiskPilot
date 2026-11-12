package com.evedenta.riskpilot.ml.data

import com.evedenta.riskpilot.domain.risk.RiskFeatures
import java.util.Random
import kotlin.math.exp

object SyntheticHistoryGenerator {
    private fun sigmoid(z: Double) = 1.0 / (1.0 + exp(-z))
    fun generate(): List<HistoricalObservation> {
        val rng = Random(42)
        return (1..300).flatMap { project ->
            // Same-project observations share a latent factor. Split projects, never individual rows.
            val projectEffect = rng.nextGaussian() * 0.3
            (1..4).map {
                val f = RiskFeatures(rng.nextDouble(), rng.nextDouble(), rng.nextDouble(), rng.nextInt(11), rng.nextDouble(), rng.nextDouble())
                val x = f.vector()
                val trueP = sigmoid(-1.6 + 1.5*x[0] - 1.4*x[1] + 1.8*x[2] + 1.1*x[3] + 1.3*x[4] - 1.2*x[5] + projectEffect)
                HistoricalObservation(project, f, if (rng.nextDouble() < trueP) 1 else 0)
            }
        }
    }
}

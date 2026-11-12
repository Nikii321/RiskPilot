package com.evedenta.riskpilot.service.ml

import com.evedenta.riskpilot.domain.risk.RiskFeatures
import com.evedenta.riskpilot.ml.data.ProjectDatasetSplitter
import com.evedenta.riskpilot.ml.data.TrainingDataset
import com.evedenta.riskpilot.ml.evaluation.BinaryClassificationEvaluator
import com.evedenta.riskpilot.ml.model.TrainingOutcome
import com.evedenta.riskpilot.ml.model.TrainingRunStatus
import com.evedenta.riskpilot.ml.training.TrainingFingerprint
import com.evedenta.riskpilot.ml.data.HistoricalObservation
import com.evedenta.riskpilot.ml.data.HistoryCsvCodec
import com.evedenta.riskpilot.ml.data.SyntheticHistoryGenerator
import com.evedenta.riskpilot.ml.inference.LogisticRegressionModel
import com.evedenta.riskpilot.ml.model.ModelReport
import com.evedenta.riskpilot.ml.model.ModelSnapshot
import com.evedenta.riskpilot.ml.training.LogisticRegressionTrainer
import com.evedenta.riskpilot.model.mapper.ModelVersionMapper
import com.evedenta.riskpilot.model.response.ml.ModelVersion
import com.evedenta.riskpilot.model.response.ml.PredictionResponse
import com.evedenta.riskpilot.repository.ml.ModelRepository
import java.time.Instant
import java.util.UUID
import org.springframework.stereotype.Service

/** Manages fitted versions. Generation, fitting and inference are separate components. */
@Service
class ModelService(private val store: ModelRepository? = null) {
    @Volatile private var active: ModelSnapshot = store?.active() ?: buildSynthetic().also { store?.save(it) }
    val report: ModelReport get() = active.report
    val observations: List<HistoricalObservation> get() = HistoryCsvCodec.parse(active.datasetCsv)
    fun snapshot(): ModelSnapshot = active
    fun activeVersion() = ModelVersionMapper.from(active)
    fun forecast(features: RiskFeatures): PredictionResponse {
        features.validate()
        val snapshot=active
        val model=LogisticRegressionModel(snapshot.report)
        val p=model.predict(features)
        return PredictionResponse(snapshot.id,p,snapshot.report.threshold,if(p>=snapshot.report.threshold) 1 else 0,model.explain(features))
    }
    fun predictor() = LogisticRegressionModel(active.report)
    fun predict(f: RiskFeatures) = predictor().predict(f)
    fun explain(f: RiskFeatures) = predictor().explain(f)
    fun csv(): String = active.datasetCsv
    fun versions(): List<ModelVersion> = (store?.versions() ?: listOf(active)).map { ModelVersionMapper.from(it) }
    private fun buildSynthetic(): ModelSnapshot {
        val rows=SyntheticHistoryGenerator.generate()
        return ModelSnapshot(UUID.randomUUID().toString(),Instant.now().toString(),"Синтетическая демонстрационная история",
            LogisticRegressionTrainer().train(rows,"Синтетические данные; учебная демонстрация, не валидация на реальных проектах"),HistoryCsvCodec.write(rows),TrainingFingerprint.of(HistoryCsvCodec.write(rows)))
    }
    @Synchronized fun train(csv: String, sourceName: String): ModelVersion {
        val rows=HistoryCsvCodec.parse(csv)
        val name=sourceName.take(120).ifBlank { "Импортированный CSV" }
        val next=ModelSnapshot(UUID.randomUUID().toString(),Instant.now().toString(),name,
            LogisticRegressionTrainer().train(rows,"Пользовательский CSV: $name; достоверность и переносимость требуют проверки"),HistoryCsvCodec.write(rows),TrainingFingerprint.of(HistoryCsvCodec.write(rows)))
        store?.save(next); active=next
        return ModelVersionMapper.from(next)
    }
    @Synchronized fun reset(): ModelVersion { val next=buildSynthetic(); store?.save(next); active=next; return ModelVersionMapper.from(next) }
    @Synchronized fun activate(id: String): ModelVersion {
        val next=(store?.versions() ?: listOf(active)).find { it.id==id } ?: throw IllegalArgumentException("Версия модели не найдена")
        store?.activate(id); active=next
        return ModelVersionMapper.from(next)
    }
    /** Scheduled promotion uses validation only; the test metrics remain informational. */
    @Synchronized fun retrain(dataset: TrainingDataset,minValidationAuc: Double,maxBrierRegression: Double): TrainingOutcome {
        if(active.trainingFingerprint==dataset.fingerprint)
            return TrainingOutcome(TrainingRunStatus.SKIPPED,active.id,"Данные и конфигурация обучения не изменились")
        val rows=HistoryCsvCodec.parse(dataset.csv)
        val next=ModelSnapshot(UUID.randomUUID().toString(),Instant.now().toString(),dataset.sourceName,
            LogisticRegressionTrainer().train(rows,dataset.provenance),dataset.csv,dataset.fingerprint)
        val validation=ProjectDatasetSplitter.split(rows).validation
        val predictor=LogisticRegressionModel(active.report)
        val prevalence=ProjectDatasetSplitter.split(rows).train.map { it.occurred }.average()
        val incumbent=BinaryClassificationEvaluator.evaluate(validation,validation.map { predictor.predict(it.features) },active.report.threshold,prevalence)
        val candidate=next.report.validationMetrics!!
        val accepted=candidate.rocAuc>=minValidationAuc && candidate.brier<=candidate.baselineBrier &&
            candidate.brier<=incumbent.brier+maxBrierRegression
        store?.save(next,activate=accepted)
        if(accepted) active=next
        val details="validation AUC=${candidate.rocAuc}, Brier=${candidate.brier}; текущая модель Brier=${incumbent.brier}"
        return TrainingOutcome(if(accepted) TrainingRunStatus.ACTIVATED else TrainingRunStatus.REJECTED,next.id,
            if(accepted) "Новая версия активирована; $details" else "Версия сохранена без активации: не пройден контроль качества; $details")
    }
}

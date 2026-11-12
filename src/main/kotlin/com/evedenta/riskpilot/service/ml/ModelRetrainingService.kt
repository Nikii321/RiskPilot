package com.evedenta.riskpilot.service.ml

import com.evedenta.riskpilot.config.ml.ModelRetrainingProperties
import com.evedenta.riskpilot.ml.data.HistoryCsvCodec
import com.evedenta.riskpilot.ml.data.ProjectDatasetSplitter
import com.evedenta.riskpilot.ml.data.TrainingDataset
import com.evedenta.riskpilot.ml.model.TrainingRun
import com.evedenta.riskpilot.ml.model.TrainingRunStatus
import com.evedenta.riskpilot.ml.training.TrainingFingerprint
import com.evedenta.riskpilot.model.response.ml.RetrainingStatusResponse
import com.evedenta.riskpilot.model.response.ml.TrainingDatasetSummary
import com.evedenta.riskpilot.repository.ml.TrainingDatasetRepository
import com.evedenta.riskpilot.repository.ml.TrainingRunRepository
import java.nio.file.Files
import java.nio.file.Path
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.UUID
import java.util.concurrent.atomic.AtomicBoolean
import org.slf4j.LoggerFactory
import org.springframework.scheduling.support.CronExpression
import org.springframework.stereotype.Service

@Service
class ModelRetrainingService(private val model: ModelService,private val datasets: TrainingDatasetRepository,
    private val runs: TrainingRunRepository,private val properties: ModelRetrainingProperties) {
    private val busy=AtomicBoolean(false)
    private val logger=LoggerFactory.getLogger(javaClass)
    @Synchronized fun stage(csv: String,sourceName: String): TrainingDatasetSummary {
        val rows=HistoryCsvCodec.parse(csv)
        ProjectDatasetSplitter.split(rows)
        val canonical=HistoryCsvCodec.write(rows)
        val name=sourceName.take(120).ifBlank { "queued-history.csv" }
        val dataset=TrainingDataset(UUID.randomUUID().toString(),name,"Пользовательский CSV: $name; достоверность и переносимость требуют проверки",
            canonical,TrainingFingerprint.of(canonical),rows.size,Instant.now().toString())
        datasets.save(dataset)
        return summary(dataset)
    }
    fun trainNow(csv: String,sourceName: String): com.evedenta.riskpilot.model.response.ml.ModelVersion {
        stage(csv,sourceName)
        return model.train(csv,sourceName)
    }
    private fun summary(dataset: TrainingDataset) = with(dataset) { TrainingDatasetSummary(id,sourceName,fingerprint,rows,uploadedAt) }
    private fun input(): TrainingDataset {
        val path=properties.sourcePath.takeIf { it.isNotBlank() }?.let { Path.of(it) }
        if(path!=null && Files.exists(path)) {
            require(Files.isRegularFile(path) && Files.size(path)<=1_000_000) { "Файл истории должен быть обычным CSV размером до 1 МБ" }
            val csv=HistoryCsvCodec.write(HistoryCsvCodec.parse(Files.readString(path)))
            val latest=datasets.latest()
            if(latest?.fingerprint!=TrainingFingerprint.of(csv)) stage(csv,path.fileName.toString())
            return datasets.latest()!!
        }
        return datasets.latest() ?: model.snapshot().let { snapshot ->
            val canonical=HistoryCsvCodec.write(HistoryCsvCodec.parse(snapshot.datasetCsv))
            TrainingDataset(snapshot.id,snapshot.sourceName,snapshot.report.provenance,canonical,
                TrainingFingerprint.of(canonical),HistoryCsvCodec.parse(canonical).size,snapshot.createdAt)
        }
    }
    fun status(): RetrainingStatusResponse {
        val enabled=properties.enabled && properties.cron!="-"
        val next=if(enabled) CronExpression.parse(properties.cron).next(ZonedDateTime.now(ZoneId.of(properties.zone)))?.toOffsetDateTime()?.toString() else null
        return RetrainingStatusResponse(enabled,properties.cron,properties.zone,next,properties.sourcePath,
            datasets.latest()?.let { summary(it) },runs.recent(),properties.minValidationAuc,properties.maxBrierRegression)
    }
    fun run(trigger: String="MANUAL"): TrainingRun {
        val started=Instant.now().toString()
        var run=TrainingRun(UUID.randomUUID().toString(),started,null,TrainingRunStatus.RUNNING,trigger,null,null,null,"Запуск переобучения")
        if(!busy.compareAndSet(false,true)) {
            return run.copy(finishedAt=Instant.now().toString(),status=TrainingRunStatus.SKIPPED,message="Уже выполняется другой запуск переобучения").also { runs.save(it) }
        }
        try {
            runs.save(run)
            val dataset=input()
            run=run.copy(sourceName=dataset.sourceName,fingerprint=dataset.fingerprint)
            runs.save(run)
            val outcome=if(runs.evaluated(dataset.fingerprint)) {
                com.evedenta.riskpilot.ml.model.TrainingOutcome(TrainingRunStatus.SKIPPED,model.snapshot().id,"Такие данные и настройки уже проверялись; повторное обучение не требуется")
            } else model.retrain(dataset,properties.minValidationAuc,properties.maxBrierRegression)
            run=run.copy(finishedAt=Instant.now().toString(),status=outcome.status,modelId=outcome.modelId,message=outcome.message)
            runs.save(run)
            logger.info("ML retraining {}: {}",run.status,run.message)
            return run
        } catch(error: Exception) {
            run=run.copy(finishedAt=Instant.now().toString(),status=TrainingRunStatus.FAILED,message=(error.message ?: error.javaClass.simpleName).take(500))
            runs.save(run)
            logger.error("ML retraining failed; active model remains available",error)
            return run
        } finally { busy.set(false) }
    }
}

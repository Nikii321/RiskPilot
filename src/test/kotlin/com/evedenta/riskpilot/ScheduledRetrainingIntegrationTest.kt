package com.evedenta.riskpilot

import com.evedenta.riskpilot.config.ml.ModelRetrainingProperties
import com.evedenta.riskpilot.ml.data.HistoryCsvCodec
import com.evedenta.riskpilot.ml.data.TrainingDataset
import com.evedenta.riskpilot.ml.model.TrainingRunStatus
import com.evedenta.riskpilot.ml.training.LogisticRegressionTrainer
import com.evedenta.riskpilot.ml.training.TrainingFingerprint
import com.evedenta.riskpilot.repository.ml.ModelRepository
import com.evedenta.riskpilot.repository.ml.TrainingDatasetRepository
import com.evedenta.riskpilot.repository.ml.TrainingRunRepository
import com.evedenta.riskpilot.scheduler.ml.ModelRetrainingScheduler
import com.evedenta.riskpilot.service.ml.ModelRetrainingService
import com.evedenta.riskpilot.service.ml.ModelService
import java.nio.file.Files
import java.time.Instant
import java.util.UUID
import kotlin.test.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.scheduling.annotation.ScheduledAnnotationBeanPostProcessor

@SpringBootTest(properties=["spring.datasource.url=jdbc:h2:mem:scheduled;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
    "riskpilot.ml.retraining.source-path=","riskpilot.ml.retraining.enabled=false"])
class ScheduledRetrainingIntegrationTest {
    @Autowired lateinit var model: ModelService
    @Autowired lateinit var jobs: ModelRetrainingService
    @Autowired lateinit var datasets: TrainingDatasetRepository
    @Autowired lateinit var runs: TrainingRunRepository
    @Autowired lateinit var store: ModelRepository
    @Autowired lateinit var scheduled: ScheduledAnnotationBeanPostProcessor
    @Test fun `scheduler is registered and queue is processed without duplicate models`() {
        assertTrue(scheduled.scheduledTasks.any { it.toString().contains("ModelRetrainingScheduler.retrain") }, scheduled.scheduledTasks.toString())
        val before=model.snapshot().id
        val rows=model.observations.map { it.copy(features=it.features.copy(complexity=it.features.complexity*.99)) }
        val dataset=jobs.stage(HistoryCsvCodec.write(rows),"queued.csv")
        assertEquals(before,model.snapshot().id,"Staging must not train or activate")
        val first=jobs.run("SCHEDULED")
        assertEquals(TrainingRunStatus.ACTIVATED,first.status,first.message)
        assertNotEquals(before,model.snapshot().id)
        assertEquals(LogisticRegressionTrainer.ENGINE,model.report.engine)
        assertEquals(dataset.fingerprint,model.snapshot().trainingFingerprint)
        assertEquals(model.snapshot().id,store.active()!!.id)
        val count=model.versions().size
        assertEquals(TrainingRunStatus.SKIPPED,jobs.run("SCHEDULED").status)
        assertEquals(count,model.versions().size)
        assertTrue(jobs.status().runs.any { it.status==TrainingRunStatus.ACTIVATED })
        assertNull(jobs.status().nextRunAt)
    }
    @Test fun `poor candidate is saved without replacing active model`() {
        val current=model.snapshot()
        val rows=model.observations.map { it.copy(features=it.features.copy(reliability=it.features.reliability*.97)) }
        val csv=HistoryCsvCodec.write(rows)
        val data=TrainingDataset(UUID.randomUUID().toString(),"candidate.csv","test",csv,TrainingFingerprint.of(csv),rows.size,Instant.now().toString())
        val outcome=model.retrain(data,1.0,0.0)
        assertEquals(TrainingRunStatus.REJECTED,outcome.status)
        assertEquals(current.id,model.snapshot().id)
        assertEquals(current.id,store.active()!!.id)
        assertTrue(model.versions().any { it.id==outcome.modelId })
    }
    @Test fun `bad source file records failure and retains active model`() {
        val file=Files.createTempFile("riskpilot-invalid-history-",".csv")
        try {
            Files.writeString(file,"bad,csv\n1,2")
            val current=model.snapshot().id
            val service=ModelRetrainingService(model,datasets,runs,ModelRetrainingProperties(sourcePath=file.toString()))
            val result=service.run("SCHEDULED")
            assertEquals(TrainingRunStatus.FAILED,result.status)
            assertEquals(current,model.snapshot().id)
            assertTrue(runs.recent().any { it.id==result.id && it.finishedAt!=null })
        } finally { Files.deleteIfExists(file) }
    }
}

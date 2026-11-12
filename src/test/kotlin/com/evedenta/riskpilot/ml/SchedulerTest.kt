package com.evedenta.riskpilot.ml

import com.evedenta.riskpilot.config.ml.ModelRetrainingProperties
import com.evedenta.riskpilot.scheduler.ml.ModelRetrainingScheduler
import com.evedenta.riskpilot.service.ml.ModelRetrainingService
import java.time.ZoneId
import java.time.ZonedDateTime
import kotlin.test.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.scheduling.support.CronExpression

class SchedulerTest {
    @Test fun `enabled tick invokes actual retraining service`() {
        val service=Mockito.mock(ModelRetrainingService::class.java)
        ModelRetrainingScheduler(service,ModelRetrainingProperties(enabled=true)).retrain()
        Mockito.verify(service).run("SCHEDULED")
    }
    @Test fun `disabled tick never starts training`() {
        val service=Mockito.mock(ModelRetrainingService::class.java)
        ModelRetrainingScheduler(service,ModelRetrainingProperties(enabled=false)).retrain()
        Mockito.verifyNoInteractions(service)
    }
    @Test fun `default cron resolves to three in Moscow`() {
        val properties=ModelRetrainingProperties()
        val before=ZonedDateTime.of(2026,11,13,2,59,0,0,ZoneId.of(properties.zone))
        assertEquals(before.withHour(3).withMinute(0),CronExpression.parse(properties.cron).next(before))
    }
}

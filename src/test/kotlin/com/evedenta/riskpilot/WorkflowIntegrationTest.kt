package com.evedenta.riskpilot

import com.evedenta.riskpilot.model.response.analysis.AnalysisResponse
import com.evedenta.riskpilot.model.response.decision.SavedDecision
import com.evedenta.riskpilot.model.response.ml.ModelVersion
import com.evedenta.riskpilot.repository.ml.ModelRepository
import com.evedenta.riskpilot.service.ml.ModelService
import java.net.URI
import java.net.http.*
import kotlin.test.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import tools.jackson.databind.ObjectMapper

@SpringBootTest(webEnvironment=SpringBootTest.WebEnvironment.RANDOM_PORT,properties=["spring.datasource.url=jdbc:h2:mem:workflows;MODE=PostgreSQL;DB_CLOSE_DELAY=-1"])
class WorkflowIntegrationTest {
    @LocalServerPort var port: Int=0
    @Autowired lateinit var mapper: ObjectMapper
    @Autowired lateinit var model: ModelService
    @Autowired lateinit var store: ModelRepository
    private fun call(path: String,method: String="GET",body: String="",contentType: String="application/json"): HttpResponse<String> =
        HttpClient.newHttpClient().send(HttpRequest.newBuilder(URI("http://localhost:$port$path"))
            .header("Content-Type",contentType).method(method,HttpRequest.BodyPublishers.ofString(body)).build(),HttpResponse.BodyHandlers.ofString())
    @Test fun `model upload activation measures and archived decision cycle`() {
        val initial=model.snapshot().id
        val csv=call("/api/history.csv").body()
        val boundary="riskpilot-test-boundary"
        val multipart="--$boundary\r\nContent-Disposition: form-data; name=\"file\"; filename=\"history.csv\"\r\nContent-Type: text/csv\r\n\r\n$csv\r\n--$boundary--\r\n"
        val trained=call("/api/model/train","POST",multipart,"multipart/form-data; boundary=$boundary")
        assertEquals(200,trained.statusCode())
        val version=mapper.readValue(trained.body(),ModelVersion::class.java)
        assertNotEquals(initial,version.id);assertEquals(version.id,store.active()!!.id)
        val exported=mapper.readValue(call("/api/model/artifact").body(),ModelVersion::class.java)
        assertEquals(version.id,exported.id)
        assertEquals(200,call("/api/model/predict","POST","""{"complexity":0.8,"experience":0.4,"workload":0.9,"dependencies":7,"volatility":0.7,"reliability":0.4}""").statusCode())
        assertEquals(200,call("/api/model/versions/$initial/activate","POST").statusCode())
        assertEquals(initial,store.active()!!.id)
        val analysis=mapper.readValue(call("/api/analysis").body(),AnalysisResponse::class.java)
        val risk=analysis.risks.first().risk.id
        val custom="""[{"title":"Тестовая мера","cost":0,"days":0,"reduction":0.9}]"""
        assertEquals(200,call("/api/risks/$risk/measures","PUT",custom).statusCode())
        val withCustom=mapper.readValue(call("/api/analysis","POST","""{"budget":0,"capacityDays":0}""").body(),AnalysisResponse::class.java)
        assertEquals("Тестовая мера",withCustom.plan.measures.single().title)
        val saved=call("/api/decisions","POST","""{"title":"Нулевой бюджет","request":{"budget":0,"capacityDays":0}}""")
        assertEquals(201,saved.statusCode())
        val left=mapper.readValue(saved.body(),SavedDecision::class.java)
        val rightResponse=call("/api/decisions","POST","""{"title":"Бюджет 300","request":{"budget":300}}""")
        val right=mapper.readValue(rightResponse.body(),SavedDecision::class.java)
        assertEquals(200,call("/api/decisions/compare?left=${left.id}&right=${right.id}").statusCode())
        val restored=mapper.readValue(call("/api/decisions/${left.id}").body(),SavedDecision::class.java)
        assertEquals(left.analysis.modelVersion.id,restored.analysis.modelVersion.id)
        assertEquals(left.analysis.plan.netBenefit,restored.analysis.plan.netBenefit)
        assertEquals(200,call("/api/risks/$risk/measures","DELETE").statusCode())
        assertEquals(204,call("/api/decisions/${left.id}","DELETE").statusCode())
        assertEquals(204,call("/api/decisions/${right.id}","DELETE").statusCode())
    }
}

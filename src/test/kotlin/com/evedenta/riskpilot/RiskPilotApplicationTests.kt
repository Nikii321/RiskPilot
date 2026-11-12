package com.evedenta.riskpilot

import java.net.URI
import java.net.http.*
import kotlin.test.*
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort

@SpringBootTest(webEnvironment=SpringBootTest.WebEnvironment.RANDOM_PORT,properties=["spring.datasource.url=jdbc:h2:mem:integration;MODE=PostgreSQL;DB_CLOSE_DELAY=-1"])
class RiskPilotApplicationTests {
    @LocalServerPort var port: Int=0
    private val client=HttpClient.newHttpClient()
    private fun call(path: String,method: String="GET",body: String=""): HttpResponse<String> {
        val req=HttpRequest.newBuilder(URI("http://localhost:$port$path")).header("Content-Type","application/json")
            .method(method,if(body.isEmpty()) HttpRequest.BodyPublishers.noBody() else HttpRequest.BodyPublishers.ofString(body)).build()
        return client.send(req,HttpResponse.BodyHandlers.ofString())
    }
    @Test fun `HTTP full decision cycle and validation`() {
        assertEquals(200,call("/").statusCode())
        val analysis=call("/api/analysis")
        assertEquals(200,analysis.statusCode()); assertContains(analysis.body(),"netBenefit")
        assertEquals(200,call("/api/model").statusCode())
        assertContains(call("/api/history.csv").body(),"project_id")
        val input="""{"title":"Тестовый риск","category":"TECHNICAL","owner":"Тестер","loss":500,"delayDays":8,"urgency":3,"features":{"complexity":0.7,"experience":0.4,"workload":0.8,"dependencies":4,"volatility":0.6,"reliability":0.5}}"""
        val created=call("/api/risks","POST",input)
        assertEquals(201,created.statusCode())
        val id=Regex("\"id\":\"([^\"]+)\"").find(created.body())!!.groupValues[1]
        assertEquals(200,call("/api/risks/$id","PUT",input).statusCode())
        assertEquals(409,call("/api/risks/$id","PUT",input).statusCode())
        assertEquals(204,call("/api/risks/$id","DELETE").statusCode())
        assertEquals(404,call("/api/risks/$id","DELETE").statusCode())
        assertEquals(400,call("/api/analysis","POST","""{"budget":-1}""").statusCode())
        assertEquals(400,call("/api/risks","POST",input.replace("500","-1")).statusCode())
        val empty=call("/api/analysis","POST","""{"budget":0,"capacityDays":0}""")
        assertEquals(200,empty.statusCode()); assertContains(empty.body(),"\"measures\":[]")
    }
}

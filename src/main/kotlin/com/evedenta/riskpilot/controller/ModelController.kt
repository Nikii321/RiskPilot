package com.evedenta.riskpilot.controller

import com.evedenta.riskpilot.model.request.ml.PredictionRequest
import com.evedenta.riskpilot.service.ml.ModelService
import com.evedenta.riskpilot.service.ml.ModelRetrainingService
import java.nio.charset.StandardCharsets
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/api")
class ModelController(private val service: ModelService,private val retraining: ModelRetrainingService) {
    @GetMapping("/model") fun current() = service.report
    @GetMapping("/model/versions") fun versions() = service.versions()
    @GetMapping("/model/artifact") fun artifact() = ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_DISPOSITION,"attachment; filename=riskpilot-model.json").body(service.activeVersion())
    @GetMapping("/history.csv",produces=["text/csv;charset=UTF-8"])
    fun history(): ResponseEntity<String> = ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_DISPOSITION,"attachment; filename=riskpilot-history.csv").body(service.csv())
    @PostMapping("/model/train",consumes=["multipart/form-data"])
    fun train(@RequestParam file: MultipartFile) = retraining.trainNow(file.bytes.toString(StandardCharsets.UTF_8),file.originalFilename ?: "uploaded.csv")
    @PostMapping("/model/reset") fun reset() = service.reset()
    @PostMapping("/model/versions/{id}/activate") fun activate(@PathVariable id: String) = service.activate(id)
    @PostMapping("/model/predict") fun predict(@RequestBody request: PredictionRequest) = service.forecast(request.toFeatures())
}

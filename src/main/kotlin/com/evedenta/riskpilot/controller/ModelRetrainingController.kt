package com.evedenta.riskpilot.controller

import com.evedenta.riskpilot.service.ml.ModelRetrainingService
import java.nio.charset.StandardCharsets
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/api/model")
class ModelRetrainingController(private val service: ModelRetrainingService) {
    @GetMapping("/retraining") fun status() = service.status()
    @PostMapping("/dataset",consumes=["multipart/form-data"])
    fun stage(@RequestParam file: MultipartFile) = service.stage(file.bytes.toString(StandardCharsets.UTF_8),file.originalFilename ?: "queued-history.csv")
    @PostMapping("/retraining/run") fun run() = service.run()
}

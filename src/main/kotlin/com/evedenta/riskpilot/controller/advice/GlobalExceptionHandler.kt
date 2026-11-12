package com.evedenta.riskpilot.controller.advice

import com.evedenta.riskpilot.exception.risk.RiskNotFoundException
import com.evedenta.riskpilot.exception.risk.RiskVersionConflictException
import org.springframework.http.*
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.annotation.*

@RestControllerAdvice
class GlobalExceptionHandler {
    @ExceptionHandler(IllegalArgumentException::class) fun invalid(e: IllegalArgumentException) =
        ResponseEntity.badRequest().body(mapOf("error" to (e.message ?: "Некорректные данные")))
    @ExceptionHandler(HttpMessageNotReadableException::class) fun unreadable() =
        ResponseEntity.badRequest().body(mapOf("error" to "Некорректный JSON: проверьте типы, обязательные поля и категорию"))
    @ExceptionHandler(RiskNotFoundException::class) fun missing(e: RiskNotFoundException) = ResponseEntity.status(404).body(mapOf("error" to e.message))
    @ExceptionHandler(RiskVersionConflictException::class) fun stale(e: RiskVersionConflictException) = ResponseEntity.status(409).body(mapOf("error" to e.message))
}

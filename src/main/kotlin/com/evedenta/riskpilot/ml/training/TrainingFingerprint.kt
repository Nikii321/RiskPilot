package com.evedenta.riskpilot.ml.training

import java.nio.charset.StandardCharsets
import java.security.MessageDigest

object TrainingFingerprint {
    fun of(canonicalCsv: String): String = MessageDigest.getInstance("SHA-256")
        .digest((LogisticRegressionTrainer.FINGERPRINT+"\n"+canonicalCsv).toByteArray(StandardCharsets.UTF_8))
        .joinToString("") { "%02x".format(it.toInt() and 255) }
}

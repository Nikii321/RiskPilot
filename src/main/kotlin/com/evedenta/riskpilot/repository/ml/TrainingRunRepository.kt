package com.evedenta.riskpilot.repository.ml

import com.evedenta.riskpilot.ml.model.TrainingRun
import com.evedenta.riskpilot.ml.model.TrainingRunStatus
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Repository
import tools.jackson.databind.ObjectMapper

@Repository
class TrainingRunRepository(private val jdbc: JdbcTemplate,private val mapper: ObjectMapper) {
    fun recent(): List<TrainingRun> = jdbc.query("SELECT payload FROM training_run ORDER BY started_at DESC LIMIT 50") { rs,_ ->
        mapper.readValue(rs.getString(1),TrainingRun::class.java) }
    fun evaluated(fingerprint: String): Boolean = (jdbc.queryForObject(
        "SELECT COUNT(*) FROM training_run WHERE fingerprint=? AND status IN ('ACTIVATED','REJECTED')",Int::class.java,fingerprint) ?: 0)>0
    fun save(run: TrainingRun) {
        val payload=mapper.writeValueAsString(run)
        if(jdbc.update("UPDATE training_run SET status=?,fingerprint=?,payload=? WHERE id=?",run.status.name,run.fingerprint,payload,run.id)==0)
            jdbc.update("INSERT INTO training_run(id,started_at,status,fingerprint,payload) VALUES (?,?,?,?,?)",run.id,run.startedAt,run.status.name,run.fingerprint,payload)
    }
}

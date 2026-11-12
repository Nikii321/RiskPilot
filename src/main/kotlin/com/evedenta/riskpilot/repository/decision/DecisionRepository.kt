package com.evedenta.riskpilot.repository.decision

import com.evedenta.riskpilot.model.response.decision.DecisionSummary
import com.evedenta.riskpilot.model.response.decision.SavedDecision
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Repository
import tools.jackson.databind.ObjectMapper

@Repository
class DecisionRepository(private val jdbc: JdbcTemplate,private val mapper: ObjectMapper) {
    fun all(): List<DecisionSummary> = jdbc.query("SELECT payload FROM decision_snapshot ORDER BY created_at DESC") { rs,_ ->
        mapper.readValue(rs.getString(1),SavedDecision::class.java).summary() }
    fun get(id: String): SavedDecision = jdbc.query("SELECT payload FROM decision_snapshot WHERE id=?",{ rs,_ ->
        mapper.readValue(rs.getString(1),SavedDecision::class.java) },id).firstOrNull() ?: throw IllegalArgumentException("Сохранённое решение не найдено")
    fun save(result: SavedDecision): SavedDecision {
        jdbc.update("INSERT INTO decision_snapshot(id,title,created_at,payload) VALUES (?,?,?,?)",result.id,result.title,result.createdAt,mapper.writeValueAsString(result))
        return result
    }
    fun delete(id: String) { require(jdbc.update("DELETE FROM decision_snapshot WHERE id=?",id)>0) { "Решение не найдено" } }
}

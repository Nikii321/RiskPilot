package com.evedenta.riskpilot.repository.mitigation

import com.evedenta.riskpilot.domain.mitigation.MitigationMeasure
import com.evedenta.riskpilot.exception.risk.RiskNotFoundException
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
class MeasureRepository(private val jdbc: JdbcTemplate) {
    fun all(): List<MitigationMeasure> = jdbc.query("SELECT * FROM custom_measure ORDER BY risk_id,cost,id") { rs,_ ->
        MitigationMeasure(rs.getString("id"),rs.getString("risk_id"),rs.getString("title"),rs.getInt("cost"),rs.getInt("days"),rs.getDouble("reduction"))
    }
    @Transactional
    fun replace(id: String,measures: List<MitigationMeasure>) {
        if(jdbc.queryForList("SELECT id FROM risk WHERE id=? FOR UPDATE",id).isEmpty()) throw RiskNotFoundException()
        jdbc.update("DELETE FROM custom_measure WHERE risk_id=?",id)
        measures.forEach { jdbc.update("INSERT INTO custom_measure(id,risk_id,title,cost,days,reduction) VALUES (?,?,?,?,?,?)",it.id,id,it.title,it.cost,it.days,it.reduction) }
    }
    @Transactional
    fun reset(id: String) = replace(id,emptyList())
}

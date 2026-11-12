package com.evedenta.riskpilot.repository.risk

import com.evedenta.riskpilot.domain.risk.Risk
import com.evedenta.riskpilot.domain.risk.RiskAssessment
import com.evedenta.riskpilot.domain.risk.RiskCategory
import com.evedenta.riskpilot.domain.risk.RiskFeatures
import com.evedenta.riskpilot.exception.risk.RiskNotFoundException
import com.evedenta.riskpilot.exception.risk.RiskVersionConflictException
import java.sql.ResultSet
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
class RiskRepository(private val jdbc: JdbcTemplate) {
    private fun map(rs: ResultSet, @Suppress("UNUSED_PARAMETER") row: Int) = Risk(rs.getString("id"), RiskAssessment(
        rs.getString("title"), RiskCategory.valueOf(rs.getString("category")),rs.getString("owner"),rs.getDouble("loss"),
        rs.getInt("delay_days"),rs.getDouble("urgency"),RiskFeatures(rs.getDouble("complexity"),rs.getDouble("experience"),
            rs.getDouble("workload"),rs.getInt("dependencies"),rs.getDouble("volatility"),rs.getDouble("reliability")),rs.getInt("version")))
    fun all() = jdbc.query("SELECT * FROM risk ORDER BY id", ::map)
    private fun values(i: RiskAssessment) = with(i) { arrayOf<Any>(title,category.name,owner,loss,delayDays,urgency,
        features.complexity,features.experience,features.workload,features.dependencies,features.volatility,features.reliability) }
    @Transactional
    fun create(input: RiskAssessment): Risk {
        input.validate()
        // Serialize writers across processes to enforce the exact optimizer's size limit.
        jdbc.queryForList("SELECT name FROM app_metadata WHERE name='seed' FOR UPDATE")
        require((jdbc.queryForObject("SELECT COUNT(*) FROM risk", Int::class.java) ?: 0) < 14) { "MVP поддерживает до 14 рисков" }
        val r=Risk.create(input.copy(version=0))
        jdbc.update("INSERT INTO risk(title,category,owner,loss,delay_days,urgency,complexity,experience,workload,dependencies,volatility,reliability,id,version) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,0)", *values(input),r.id)
        return r
    }
    fun update(id: String, input: RiskAssessment): Risk {
        input.validate()
        val count=jdbc.update("UPDATE risk SET title=?,category=?,owner=?,loss=?,delay_days=?,urgency=?,complexity=?,experience=?,workload=?,dependencies=?,volatility=?,reliability=?,version=version+1 WHERE id=? AND version=?", *values(input),id,input.version)
        if(count==0) { if(all().none { it.id==id }) throw RiskNotFoundException(); throw RiskVersionConflictException() }
        return Risk(id,input.copy(version=input.version+1))
    }
    fun delete(id: String) { if(jdbc.update("DELETE FROM risk WHERE id=?",id)==0) throw RiskNotFoundException() }
    @Transactional
    fun initializeDemoIfAbsent(inputs: List<RiskAssessment>) {
        if(jdbc.queryForObject("SELECT COUNT(*) FROM app_metadata WHERE name='seed'", Int::class.java) == 0) {
            jdbc.update("INSERT INTO app_metadata(name,meta_value) VALUES ('seed','v1')")
            inputs.forEach { create(it) }
        }
    }
}

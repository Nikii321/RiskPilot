package com.evedenta.riskpilot.repository.ml

import com.evedenta.riskpilot.ml.model.ModelSnapshot
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import tools.jackson.databind.ObjectMapper

@Repository
class ModelRepository(private val jdbc: JdbcTemplate,private val mapper: ObjectMapper) {
    fun versions(): List<ModelSnapshot> = jdbc.query("SELECT payload FROM model_version ORDER BY created_at DESC") { rs,_ -> mapper.readValue(rs.getString(1),ModelSnapshot::class.java) }
    fun active(): ModelSnapshot? = jdbc.query("SELECT m.payload FROM model_version m JOIN app_metadata a ON a.meta_value=m.id WHERE a.name='active_model'") { rs,_ -> mapper.readValue(rs.getString(1),ModelSnapshot::class.java) }.firstOrNull()
    @Transactional
    fun save(model: ModelSnapshot, activate: Boolean = true) {
        jdbc.update("INSERT INTO model_version(id,created_at,payload) VALUES (?,?,?)",model.id,model.createdAt,mapper.writeValueAsString(model))
        if(activate) activate(model.id)
    }
    @Transactional
    fun activate(id: String) {
        require(jdbc.queryForObject("SELECT COUNT(*) FROM model_version WHERE id=?",Int::class.java,id)==1) { "Версия модели не найдена" }
        if(jdbc.update("UPDATE app_metadata SET meta_value=? WHERE name='active_model'",id)==0)
            jdbc.update("INSERT INTO app_metadata(name,meta_value) VALUES ('active_model',?)",id)
    }
}

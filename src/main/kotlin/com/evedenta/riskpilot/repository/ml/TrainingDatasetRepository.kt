package com.evedenta.riskpilot.repository.ml

import com.evedenta.riskpilot.ml.data.TrainingDataset
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import tools.jackson.databind.ObjectMapper

@Repository
class TrainingDatasetRepository(private val jdbc: JdbcTemplate,private val mapper: ObjectMapper) {
    fun latest(): TrainingDataset? = jdbc.query("SELECT payload FROM training_dataset WHERE id=1") { rs,_ ->
        mapper.readValue(rs.getString(1),TrainingDataset::class.java) }.firstOrNull()
    @Transactional fun save(dataset: TrainingDataset) {
        if(jdbc.update("UPDATE training_dataset SET payload=? WHERE id=1",mapper.writeValueAsString(dataset))==0)
            jdbc.update("INSERT INTO training_dataset(id,payload) VALUES (1,?)",mapper.writeValueAsString(dataset))
    }
}

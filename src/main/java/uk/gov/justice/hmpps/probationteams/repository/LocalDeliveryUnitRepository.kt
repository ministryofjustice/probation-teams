package uk.gov.justice.hmpps.probationteams.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import uk.gov.justice.hmpps.probationteams.model.LocalDeliveryUnit
import java.util.*

@Repository
interface LocalDeliveryUnitRepository : JpaRepository<LocalDeliveryUnit, UUID> {
    @Query("select distinct probationAreaCode from LocalDeliveryUnit order by probationAreaCode")
    fun getProbationAreaCodes(): List<String>
    fun findByProbationAreaCode(probationAreaCode: String): List<LocalDeliveryUnit>
    fun findByProbationAreaCodeAndLocalDeliveryUnitCode(probationAreaCode: String, localDeliveryUnitCode: String): Optional<LocalDeliveryUnit>
}
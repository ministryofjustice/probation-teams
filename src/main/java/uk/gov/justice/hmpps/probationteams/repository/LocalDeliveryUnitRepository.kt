package uk.gov.justice.hmpps.probationteams.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import uk.gov.justice.hmpps.probationteams.model.LocalDeliveryUnit
import java.util.*

@Repository
interface LocalDeliveryUnitRepository : JpaRepository<LocalDeliveryUnit, UUID> {
    fun findByProbationAreaCodeAndLocalDeliveryUnitCode(probationAreaCode: String, localDeliveryUnitCode: String): Optional<LocalDeliveryUnit>
}
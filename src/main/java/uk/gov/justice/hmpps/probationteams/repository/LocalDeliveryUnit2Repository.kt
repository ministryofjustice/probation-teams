package uk.gov.justice.hmpps.probationteams.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import uk.gov.justice.hmpps.probationteams.model.LocalDeliveryUnit2
import java.util.*

@Repository
interface LocalDeliveryUnit2Repository : JpaRepository<LocalDeliveryUnit2, UUID> {
    fun findByProbationAreaCodeAndLocalDeliveryUnitCode(probationAreaCode: String, localDeliveryUnitCode: String): Optional<LocalDeliveryUnit2>
}
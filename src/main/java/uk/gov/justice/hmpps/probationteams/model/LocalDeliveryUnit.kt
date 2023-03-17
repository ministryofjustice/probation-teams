package uk.gov.justice.hmpps.probationteams.model

import jakarta.persistence.CollectionTable
import jakarta.persistence.Column
import jakarta.persistence.ElementCollection
import jakarta.persistence.Entity
import jakarta.persistence.EntityListeners
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.MapKeyColumn
import jakarta.persistence.Table
import org.hibernate.annotations.GenericGenerator
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime
import java.util.UUID

@Entity
@EntityListeners(AuditingEntityListener::class)
@Table(name = "LOCAL_DELIVERY_UNIT2")
data class LocalDeliveryUnit(

  @Column(nullable = false)
  var probationAreaCode: String,

  @Column(nullable = false)
  var localDeliveryUnitCode: String,

  @Column
  var functionalMailbox: String? = null,

  /**
   * Map of probation team code to ProbationTeam
   */
  @ElementCollection(fetch = FetchType.EAGER)
  @CollectionTable(
    name = "PROBATION_TEAM",
    joinColumns = [JoinColumn(name = "LOCAL_DELIVERY_UNIT_ID")],
  )
  @MapKeyColumn(name = "TEAM_CODE")
  var probationTeams: MutableMap<String, ProbationTeam> = mutableMapOf(),
) {

  @Id
  @GeneratedValue(generator = "UUID")
  @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
  @Column(name = "LOCAL_DELIVERY_UNIT_ID", updatable = false, nullable = false)
  var id: UUID? = null

  @CreatedDate
  @Column(nullable = false)
  var createDateTime: LocalDateTime? = null

  @CreatedBy
  @Column(nullable = false)
  var createUserId: String? = null

  @LastModifiedDate
  var modifyDateTime: LocalDateTime? = null

  @LastModifiedBy
  var modifyUserId: String? = null
}

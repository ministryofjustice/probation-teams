package uk.gov.justice.hmpps.probationteams.model

import jakarta.persistence.Column
import jakarta.persistence.Embeddable

@Embeddable
data class ProbationTeam(@Column(nullable = false) var functionalMailbox: String)

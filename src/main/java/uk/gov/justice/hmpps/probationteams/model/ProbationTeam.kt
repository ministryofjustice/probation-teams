package uk.gov.justice.hmpps.probationteams.model

import javax.persistence.Column
import javax.persistence.Embeddable

@Embeddable
data class ProbationTeam(@Column(nullable = false) var functionalMailbox: String)

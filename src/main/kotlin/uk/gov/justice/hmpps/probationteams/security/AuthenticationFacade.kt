package uk.gov.justice.hmpps.probationteams.security

interface AuthenticationFacade {
  fun getCurrentUsername(): String?
}

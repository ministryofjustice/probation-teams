package uk.gov.justice.hmpps.probationteams.utils

import java.time.Duration

data class JwtParameters(
  val username: String?,
  val userId: String? = null,
  val scope: List<String>,
  val roles: List<String>,
  val expiryTime: Duration
)

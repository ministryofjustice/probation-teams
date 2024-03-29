package uk.gov.justice.hmpps.probationteams.utils

import jakarta.servlet.Filter
import jakarta.servlet.FilterChain
import jakarta.servlet.FilterConfig
import jakarta.servlet.ServletException
import jakarta.servlet.ServletRequest
import jakarta.servlet.ServletResponse
import org.slf4j.MDC
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import uk.gov.justice.hmpps.probationteams.security.UserSecurityUtils
import java.io.IOException

@Component
@Order(1)
class UserMdcFilter @Autowired constructor(private val userSecurityUtils: UserSecurityUtils) : Filter {
  override fun init(filterConfig: FilterConfig) {
    // Initialise - no functionality
  }

  @Throws(IOException::class, ServletException::class)
  override fun doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) {
    val currentUsername = userSecurityUtils.getCurrentUsername()
    try {
      MDC.put(USER_ID_HEADER, currentUsername)
      chain.doFilter(request, response)
    } finally {
      MDC.remove(USER_ID_HEADER)
    }
  }

  override fun destroy() {
    // Destroy - no functionality
  }

  companion object {
    private const val USER_ID_HEADER = "userId"
  }
}

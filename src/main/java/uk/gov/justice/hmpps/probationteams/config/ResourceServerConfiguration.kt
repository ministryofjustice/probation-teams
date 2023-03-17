package uk.gov.justice.hmpps.probationteams.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.convert.converter.Converter
import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy.STATELESS
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter
import org.springframework.security.web.SecurityFilterChain

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true, proxyTargetClass = true)
class ResourceServerConfiguration {
  @Bean
  fun securityFilterChain(http: HttpSecurity): SecurityFilterChain =
    http
      .sessionManagement()
      .sessionCreationPolicy(STATELESS) // Can't have CSRF protection as requires session
      .and().csrf().disable()
      .authorizeHttpRequests { auth ->
        auth
          .requestMatchers(
            "/webjars/**",
            "/favicon.ico",
            "/health/**",
            "/info",
            "/ping",
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-ui.html"
          )
          .permitAll()
          .anyRequest()
          .authenticated()
      }
      .also {
        it.oauth2ResourceServer().jwt().jwtAuthenticationConverter(AuthAwareTokenConverter())
      }.build()
}

class AuthAwareTokenConverter : Converter<Jwt, AbstractAuthenticationToken> {
  private val jwtGrantedAuthoritiesConverter: Converter<Jwt, Collection<GrantedAuthority>> =
    JwtGrantedAuthoritiesConverter()

  override fun convert(jwt: Jwt): AbstractAuthenticationToken {
    val claims: Map<String, Any> = jwt.claims
    val principal: String = findPrincipal(claims)
    val authorities = extractAuthorities(jwt)

    return AuthAwareAuthenticationToken(jwt, principal, authorities)
  }

  private fun findPrincipal(claims: Map<String, Any?>): String =
    if (claims.containsKey("user_name")) {
      claims["user_name"] as String
    } else {
      claims["client_id"] as String
    }

  private fun extractAuthorities(jwt: Jwt): Collection<GrantedAuthority> {
    val authorities =
      mutableListOf<GrantedAuthority>().apply { addAll(jwtGrantedAuthoritiesConverter.convert(jwt)!!) }
    if (jwt.claims.containsKey("authorities")) {
      @Suppress("UNCHECKED_CAST")
      val claimAuthorities = (jwt.claims["authorities"] as Collection<String>).toList()
      authorities.addAll(claimAuthorities.map(::SimpleGrantedAuthority))
    }
    return authorities.toSet()
  }
}

class AuthAwareAuthenticationToken(
  jwt: Jwt,
  principal: String,
  authorities: Collection<GrantedAuthority>,
) : JwtAuthenticationToken(jwt, authorities) {
  private val privatePrincipal: Any = principal
  override fun getPrincipal(): Any = privatePrincipal
  override fun getName(): String = principal.toString()
}

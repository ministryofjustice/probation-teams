package uk.gov.justice.hmpps.probationteams.utils

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import org.apache.commons.codec.binary.Base64
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.ByteArrayResource
import org.springframework.security.oauth2.provider.token.store.KeyStoreKeyFactory
import org.springframework.stereotype.Component
import java.security.KeyPair
import java.time.Duration
import java.util.*

@Component
class JwtAuthenticationHelper(@Value("\${jwt.signing.key.pair}") privateKeyPair: String,
                              @Value("\${jwt.keystore.password}") keystorePassword: String,
                              @Value("\${jwt.keystore.alias:elite2api}") keystoreAlias: String) {
    private val keyPair: KeyPair

    init {
        val keyStoreKeyFactory = KeyStoreKeyFactory(ByteArrayResource(Base64.decodeBase64(privateKeyPair)),
                keystorePassword.toCharArray())
        keyPair = keyStoreKeyFactory.getKeyPair(keystoreAlias)
    }

    fun createJwt(parameters: JwtParameters): String = with(parameters) {
        val claims = HashMap<String, Any?>()
        if (username != null) claims["user_name"] = username
        if (userId != null) claims["user_id"] = userId
        claims["client_id"] = "elite2apiclient"
        if (roles.isNotEmpty()) claims["authorities"] = roles
        if (scope.isNotEmpty()) claims["scope"] = scope
        Jwts.builder()
                .setId(UUID.randomUUID().toString())
                .setSubject(username)
                .addClaims(claims)
                .setExpiration(Date(System.currentTimeMillis() + expiryTime.toMillis()))
                .signWith(SignatureAlgorithm.RS256, keyPair.private)
                .compact()
    }
}
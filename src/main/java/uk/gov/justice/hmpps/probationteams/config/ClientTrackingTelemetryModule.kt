package uk.gov.justice.hmpps.probationteams.config

import com.microsoft.applicationinsights.TelemetryConfiguration
import com.microsoft.applicationinsights.extensibility.TelemetryModule
import com.microsoft.applicationinsights.web.extensibility.modules.WebTelemetryModule
import com.microsoft.applicationinsights.web.internal.ThreadContext
import io.jsonwebtoken.Claims
import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.Jwts
import org.apache.commons.codec.binary.Base64
import org.apache.commons.lang3.StringUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpHeaders
import java.security.GeneralSecurityException
import java.security.KeyFactory
import java.security.interfaces.RSAPublicKey
import java.security.spec.X509EncodedKeySpec
import java.util.*
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse
import javax.servlet.http.HttpServletRequest

@Configuration
class ClientTrackingTelemetryModule @Autowired constructor(
        @param:Value("\${jwt.public.key}") val jwtPublicKey: String) : WebTelemetryModule, TelemetryModule {

    override fun onBeginRequest(req: ServletRequest, res: ServletResponse) {
        val httpServletRequest = req as HttpServletRequest
        val token = httpServletRequest.getHeader(HttpHeaders.AUTHORIZATION)
        val bearer = "Bearer "
        if (StringUtils.startsWithIgnoreCase(token, bearer)) {
            try {
                val jwtBody = getClaimsFromJWT(token)
                val properties = ThreadContext.getRequestTelemetryContext().httpRequestTelemetry.properties
                val user = Optional.ofNullable(jwtBody["user_name"])
                user.map { obj: Any -> obj.toString() }.ifPresent { u: String -> properties["username"] = u }
                properties["clientId"] = jwtBody["client_id"].toString()
            } catch (e: ExpiredJwtException) { // Expired token which spring security will handle
            } catch (e: GeneralSecurityException) {
                log.warn("problem decoding jwt public key for application insights", e)
            }
        }
    }

    @Throws(ExpiredJwtException::class, GeneralSecurityException::class)
    private fun getClaimsFromJWT(token: String): Claims =
            Jwts.parser()
                    .setSigningKey(getPublicKeyFromString(jwtPublicKey))
                    .parseClaimsJws(token.substring(7))
                    .body


    @Throws(GeneralSecurityException::class)
    fun getPublicKeyFromString(key: String?): RSAPublicKey {
        val publicKey = String(Base64.decodeBase64(key))
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replace("\\R".toRegex(), "")
        val encoded = Base64.decodeBase64(publicKey)
        return KeyFactory.getInstance("RSA").generatePublic(X509EncodedKeySpec(encoded)) as RSAPublicKey
    }

    override fun onEndRequest(req: ServletRequest, res: ServletResponse) {}
    override fun initialize(configuration: TelemetryConfiguration) {}

    companion object {
        val log: Logger = LoggerFactory.getLogger(ClientTrackingTelemetryModule::class.java)
    }
}
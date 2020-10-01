package uk.gov.justice.hmpps.probationteams.controllers

import org.springframework.boot.info.BuildProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import springfox.documentation.builders.AuthorizationCodeGrantBuilder
import springfox.documentation.builders.OAuthBuilder
import springfox.documentation.builders.PathSelectors
import springfox.documentation.builders.RequestHandlerSelectors
import springfox.documentation.service.ApiInfo
import springfox.documentation.service.AuthorizationScope
import springfox.documentation.service.Contact
import springfox.documentation.service.SecurityReference
import springfox.documentation.service.SecurityScheme
import springfox.documentation.service.StringVendorExtension
import springfox.documentation.service.VendorExtension
import springfox.documentation.spi.DocumentationType
import springfox.documentation.spi.service.contexts.SecurityContext
import springfox.documentation.spring.web.plugins.Docket
import springfox.documentation.swagger.web.DocExpansion
import springfox.documentation.swagger.web.ModelRendering
import springfox.documentation.swagger.web.OperationsSorter
import springfox.documentation.swagger.web.TagsSorter
import springfox.documentation.swagger.web.UiConfiguration
import springfox.documentation.swagger.web.UiConfigurationBuilder
import java.time.LocalDateTime
import java.time.ZonedDateTime
import java.util.ArrayList
import java.util.Date
import java.util.Optional


@Configuration
class SwaggerConfiguration(buildProperties: BuildProperties) {
    private val version: String = buildProperties.version

    @Bean
    fun api(): Docket {
        val docket = Docket(DocumentationType.SWAGGER_2)
                .select()
                .apis(RequestHandlerSelectors.basePackage("uk.gov.justice.hmpps.probationteams.controllers"))
                .paths(PathSelectors.any())
                .build()
                .securitySchemes(listOf(securityScheme()))
                .securityContexts(listOf(securityContext()))
                .apiInfo(apiInfo())
        docket.genericModelSubstitutes(Optional::class.java)
        docket.directModelSubstitute(ZonedDateTime::class.java, Date::class.java)
        docket.directModelSubstitute(LocalDateTime::class.java, Date::class.java)
        return docket
    }

    private fun securityScheme(): SecurityScheme {
        val grantType = AuthorizationCodeGrantBuilder()
                .tokenEndpoint { it.url("http://localhost:9090/auth/oauth/token").tokenName("oauthtoken") }
                .tokenRequestEndpoint {
                    it.url("http://localhost:9090/auth/oauth/authorize").clientIdName("swagger-client").clientSecretName("clientsecret")
                }
                .build()
        return OAuthBuilder().name("spring_oauth")
                .grantTypes(listOf(grantType))
                .scopes(listOf(*scopes()))
                .build()
    }

    private fun scopes() = arrayOf(
            AuthorizationScope("read", "for read operations"),
            AuthorizationScope("write", "for write operations")
    )


    private fun securityContext() = SecurityContext.builder()
            .securityReferences(listOf(SecurityReference("spring_oauth", scopes())))
            .build()


    private fun contactInfo() = Contact(
            "HMPPS Digital Studio",
            "",
            "feedback@digital.justice.gov.uk")


    private fun apiInfo(): ApiInfo {
        val vendorExtension = StringVendorExtension("", "")
        val vendorExtensions: MutableCollection<VendorExtension<*>> = ArrayList()
        vendorExtensions.add(vendorExtension)
        return ApiInfo(
                "HMPPS Probation Teams Documentation",
                "Reference data API for probation teams.",
                version,
                "https://gateway.nomis-api.service.justice.gov.uk/auth/terms",
                contactInfo(),
                "MIT", "https://opensource.org/licenses/MIT", vendorExtensions)
    }

    @Bean
    fun uiConfig(): UiConfiguration? {
        return UiConfigurationBuilder.builder()
                .deepLinking(true)
                .displayOperationId(false)
                .defaultModelsExpandDepth(3)
                .defaultModelExpandDepth(3)
                .defaultModelRendering(ModelRendering.EXAMPLE)
                .displayRequestDuration(false)
                .docExpansion(DocExpansion.LIST)
                .filter(false)
                .maxDisplayedTags(null)
                .operationsSorter(OperationsSorter.ALPHA)
                .showExtensions(false)
                .showCommonExtensions(false)
                .tagsSorter(TagsSorter.ALPHA)
                .supportedSubmitMethods(UiConfiguration.Constants.DEFAULT_SUBMIT_METHODS)
                .validatorUrl(null)
                .build()
    }
}
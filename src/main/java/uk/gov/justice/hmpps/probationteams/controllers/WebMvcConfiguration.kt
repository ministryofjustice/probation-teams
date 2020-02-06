package uk.gov.justice.hmpps.probationteams.controllers

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.converter.HttpMessageConverter
import org.springframework.http.converter.StringHttpMessageConverter
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class WebMvcConfiguration {
    /**
     * Remove any StringHttpMessageConverters from the set configured by Spring Boot.  Now @RestControllers will correctly encode a response of type String as JSON string
     * @return The default List of HttpMessageConverter minus any StringHttpMessageConverter instances.
     */
    @Bean
    fun messageConverterReconfigurer(): WebMvcConfigurer = object : WebMvcConfigurer {
        override fun extendMessageConverters(converters: MutableList<HttpMessageConverter<*>>) {
            val filtered = converters.filter { it !is StringHttpMessageConverter }
            converters.clear()
            converters.addAll(filtered)
        }
    }
}

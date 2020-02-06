package uk.gov.justice.hmpps.probationteams.controllers;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;
import java.util.stream.Collectors;


@Configuration
public class WebMvcConfiguration {


    /**
     * Remove any StringHttpMessageConverters from the set configured by Spring Boot.  Now @RestControllers will correctly encode a response of type String as JSON string
     * @return The default List of HttpMessageConverter minus any StringHttpMessageConverter instances.
     */
    @Bean
    public WebMvcConfigurer messageConverterReconfigurer() {
        return new WebMvcConfigurer() {

            @Override
            public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
                final var filtered = converters.stream().filter(c -> !(c instanceof StringHttpMessageConverter)).collect(Collectors.toList());
                converters.clear();
                converters.addAll(filtered);
            }
        };
    }
}

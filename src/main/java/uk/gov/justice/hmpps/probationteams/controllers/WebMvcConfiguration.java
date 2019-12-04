package uk.gov.justice.hmpps.probationteams.controllers;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.data.web.SortHandlerMethodArgumentResolver;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

import java.util.List;

@Configuration
public class WebMvcConfiguration extends WebMvcConfigurationSupport {
    /**
     * Unregister the default {@link StringHttpMessageConverter} as we want Strings
     * to be handled by the JSON converter.
     *
     * @param converters List of already configured converters
     * @see WebMvcConfigurationSupport#addDefaultHttpMessageConverters(List)
     */
    @Override
    protected void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        converters.stream()
                .filter(c -> c instanceof StringHttpMessageConverter)
                .findFirst()
                .ifPresent(converters::remove);
    }

    /**
     * But now Spring doesn't use the method argument resolvers for Pageable and Sort.
     * Add custom PageableHandlerMethodArgumentResolver and SortHandlerMethodArgumentResolver
     * to the ones registered by default.
     * @param argumentResolvers add resolvers to this list.
     */
    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
        argumentResolvers.add( new PageableHandlerMethodArgumentResolver());
        argumentResolvers.add(new SortHandlerMethodArgumentResolver());
    }

}
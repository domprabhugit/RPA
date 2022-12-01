/*
 * Robotic Process Automation
 * @originalAuthor Dominic D Prabhu
 * Copyright(c) 2017, www.tekplay.com
 */

package com.rpa.camel.config;

import org.apache.camel.CamelContext;
import org.apache.camel.ConsumerTemplate;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.RoutesBuilder;
import org.apache.camel.TypeConverter;
import org.apache.camel.spring.SpringCamelContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Configuration
@Component
public class CamelAutoConfiguration {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired(required = false)
    private RoutesBuilder[] routesBuilders;
    
    /**
     * Spring-aware Camel context for the application. Auto-detects and loads all routes available in the Spring
     * context.
     */
    @Bean
    public CamelContext camelContext() throws Exception {
    	SpringCamelContext camelContext = new SpringCamelContext(applicationContext);
        if (routesBuilders != null) {
            for (RoutesBuilder routesBuilder : routesBuilders) {
                camelContext.addRoutes(routesBuilder);
            }
        }
        return camelContext;
    }

    /**
     * Default producer template for the bootstrapped Camel context.
     */
    @Bean
    public ProducerTemplate producerTemplate() throws Exception {
        return camelContext().createProducerTemplate();
    }

    /**
     * Default consumer template for the bootstrapped Camel context.
     */
    @Bean
    public ConsumerTemplate consumerTemplate() throws Exception {
        return camelContext().createConsumerTemplate();
    }

    @Bean
    public TypeConverter typeConverter() throws Exception {
        return camelContext().getTypeConverter();
    }
}
package com.negling.camunda.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.jsontype.DefaultBaseTypeLimitingValidator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.ClassUtils;

import static com.fasterxml.jackson.databind.ObjectMapper.DefaultTyping.NON_FINAL;

@Configuration
public class ObjectMapperConfig {
    private static final boolean JDK8_MODULE_PRESENT =
            ClassUtils.isPresent("com.fasterxml.jackson.datatype.jdk8.Jdk8Module", null);

    private static final boolean JAVA_TIME_MODULE_PRESENT =
            ClassUtils.isPresent("com.fasterxml.jackson.datatype.jsr310.JavaTimeModule", null);

    /**
     * ObjectMapper that can correctly serialize/deserialize successors of HistoryEvent base class
     */
    @Bean("polymorphicObjectMapper")
    public ObjectMapper objectMapper() {
        var objectMapper = JsonMapper.builder()
                .configure(MapperFeature.DEFAULT_VIEW_INCLUSION, false)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .activateDefaultTyping(new DefaultBaseTypeLimitingValidator(), NON_FINAL)
                .build();

        registerWellKnownModulesIfAvailable(objectMapper);

        return objectMapper;
    }

    private static void registerWellKnownModulesIfAvailable(ObjectMapper objectMapper) {
        if (JDK8_MODULE_PRESENT) {
            objectMapper.registerModule(Jdk8ModuleProvider.MODULE);
        }

        if (JAVA_TIME_MODULE_PRESENT) {
            objectMapper.registerModule(JavaTimeModuleProvider.MODULE);
        }
    }

    private static final class Jdk8ModuleProvider {
        static final com.fasterxml.jackson.databind.Module MODULE =
                new com.fasterxml.jackson.datatype.jdk8.Jdk8Module();
    }

    private static final class JavaTimeModuleProvider {
        static final com.fasterxml.jackson.databind.Module MODULE =
                new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule();
    }
}

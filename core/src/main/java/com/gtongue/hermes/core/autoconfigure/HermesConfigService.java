package com.gtongue.hermes.core.autoconfigure;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gtongue.hermes.di.injection.InjectableClass;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@InjectableClass
@Slf4j
public class HermesConfigService {

    private final Map<String, Object> config;
    private final ObjectMapper mapper;

    public HermesConfigService() {
        log.info("Loading config from hermes.config.json");
        this.mapper = new ObjectMapper();
        Map<String, Object> configValues = new HashMap<>();
        try (InputStream configInputStream = HermesConfigService.class.getClassLoader().getResourceAsStream("hermes.config.json")) {
            if (Objects.nonNull(configInputStream)) {
                configValues = mapper.readValue(configInputStream, new TypeReference<>() {
                });
                log.info("Loaded configuration {}", configValues);
            }
        } catch (IOException e) {
            log.error("Error loading hermes.config.json", e);
        }
        config = configValues;
    }

    public <T> T getConfigurationValue(String configurationKey, Class<T> typeClass) {
        if (Objects.isNull(configurationKey) || configurationKey.equals("")) {
            return null;
        }
        String[] pathToResult = configurationKey.split("\\.");
        Map<String, Object> currentLookupLocation = config;
        for (int i = 0; i < pathToResult.length; i++) {
            String currentPathPart = pathToResult[i];
            if (!currentLookupLocation.containsKey(currentPathPart)) {
                throw new RuntimeException("Key " + configurationKey + " not found");
            }
            if (i == pathToResult.length - 1) {
                if (typeClass == String.class) {
                    Object value = currentLookupLocation.get(currentPathPart);
                    if (value.getClass().isAssignableFrom(typeClass)) {
                        return typeClass.cast(value);
                    } else {
                        return null;
                    }
                }
                try {
                    return this.mapper.readValue(currentLookupLocation.get(currentPathPart).toString(), typeClass);
                } catch (Exception e) {
                    throw new RuntimeException("Unable to read value for " + configurationKey, e);
                }
            }
//          TODO: Fix this.
            currentLookupLocation = (Map<String, Object>) currentLookupLocation.get(currentPathPart);
        }
        throw new RuntimeException("Key " + configurationKey + " not found");
    }

    public <T> T getConfigurationValueWithDefault(String configurationKey, Class<T> typeClass, T defaultValue) {
        if (Objects.isNull(configurationKey) || configurationKey.equals("")) {
            return defaultValue;
        }
        String[] pathToResult = configurationKey.split("\\.");
        Map<String, Object> currentLookupLocation = config;
        for (int i = 0; i < pathToResult.length; i++) {
            String currentPathPart = pathToResult[i];
            if (!currentLookupLocation.containsKey(currentPathPart)) {
                return defaultValue;
            }
            if (i == pathToResult.length - 1) {
                try {
                    return this.mapper.readValue(currentLookupLocation.get(currentPathPart).toString(), typeClass);
                } catch (Exception e) {
                    log.error("Unable to read value for {}", configurationKey, e);
                    return defaultValue;
                }
            }
//          TODO: Fix this same as above.
            currentLookupLocation = (Map<String, Object>) currentLookupLocation.get(currentPathPart);
        }
        return defaultValue;
    }
}

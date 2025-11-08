package com.konorkestra.center.parser;

import com.konorkestra.center.parser.exceptions.IdExistsException;
import com.konorkestra.center.service.ConfigSetValidator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.junit.jupiter.api.Assertions.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.any;

/**
 * Input: List of Maps representing ConfigSets
 * Basic check for MVP: uniqueness of UID, key value pairs are not null
 * Return List<ConfigSet>
 * Throws NullConfigValueException | IdExistsException
 * Mocks: Validator
 */
@ExtendWith(MockitoExtension.class)
public class ConfigSetParserTest {
    
    @Mock private ConfigSetValidator validator;
    private ConfigSetParser configSetParser;
    private static final Logger logger = LoggerFactory.getLogger(ConfigSetParserTest.class);
    @BeforeEach
    public void setup(){
        Mockito.when(validator.validate(any())).thenReturn(true);
        configSetParser = new ConfigSetParser(validator);
    }
    @ParameterizedTest
    @MethodSource("provideMaps")
    void testParsingCorrect(List<Map<String,Object>> input){
        Assertions.assertNotNull(configSetParser.parse(input));
    }
    @ParameterizedTest
    @MethodSource("provideMaps")
    void testParsingExisting(List<Map<String,Object>> input){
        Mockito.when(validator.validate(any())).thenReturn(false);
        Assertions.assertThrows(IdExistsException.class, () -> configSetParser.parse(input));
    }
    static Stream<List<Map<String,Object>>> provideMaps() {
        return Stream.of(
                List.of(
                        Map.of("uid", "configset1", "configs", Map.of("max_connections", "100", "enable_logging", "true"))
                ),
                List.of(
                        Map.of("uid", "configset2", "configs", Map.of())
                )
        );
    }
}

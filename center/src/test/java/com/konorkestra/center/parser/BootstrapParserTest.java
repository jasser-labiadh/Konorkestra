package com.konorkestra.center.parser;
import com.konorkestra.center.parser.exceptions.ParserNotFoundException;
import com.konorkestra.center.parser.exceptions.ParsingException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.provider.ValueSources;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
@ExtendWith(MockitoExtension.class)
public class BootstrapParserTest {
    @Mock private GroupParser groupParser;
    @Mock private PolicyParser policyParser;
    @Mock private ConfigSetParser configSetParser;
    private BootstrapParser bootstrapParser;
    @BeforeEach
    void setup(){
        lenient().when(groupParser.getHandledKey()).thenReturn("groups");
        lenient().when(policyParser.getHandledKey()).thenReturn("policies");
        lenient().when(configSetParser.getHandledKey()).thenReturn("configsets");
        lenient().when(groupParser.parse(any())).thenReturn(true);
        lenient().when(policyParser.parse(any())).thenReturn(true);
        lenient().when(configSetParser.parse(any())).thenReturn(true);
        bootstrapParser = new BootstrapParser(List.of(groupParser, policyParser, configSetParser));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            """
                    Groups:
                      - uid: group1
                        configsets:
                          - configset1
                          - configset2
                        policy: gradual_policy_1
                    
                      - uid: group2
                        configsets:
                          - configset3
                        policy: all_policy
                    
                    Configsets:
                      - uid: configset1
                        configs:
                          max_connections: "100"
                          enable_logging: "true"
                    
                      - uid: configset2
                        configs:
                          timeout: "30"
                          retry_count: "5"
                    
                      - uid: configset3
                        configs:
                          feature_flag_x: "enabled"
                    
                    Policies:
                      - uid: all_policy
                        type: linear
                        parameters: {}
                    
                      - uid: gradual_policy_1
                        type: gradual
                        parameters:
                          max_nodes: 2
                          constraint: "cpu < 80"
                    
                      - uid: quorum_policy
                        type: quorum
                        parameters:
                          required_nodes: 3
                    """,
            
    }) 
    void testParsingCorrectYaml(String yaml){
        assertTrue(bootstrapParser.parse(yaml));
    }
    @ParameterizedTest
    @ValueSource(strings = {
            """
                        groups:
                          - configsets:
                              - configset1
                            policy: gradual_policy_1
                    
                        configsets:
                          - uid: configset1
                            configs:
                              max_connections: ""
                              enable_logging: "true"
                    
                        policies:
                          - uid: gradual_policy_1
                            type: unknown_type
                            parameters:
                              max_nodes: 2
                              constraint: "cpu < 80"
                    
                        unknownSection:
                          - something: value
                    """
    })
    void testParsingIncorrectYaml(String yaml){
        Assertions.assertThrows(ParserNotFoundException.class, () -> bootstrapParser.parse(yaml));
    }
    @Test
    void testParsingNonMapYaml(){
        Assertions.assertThrows(ParsingException.class, () -> bootstrapParser.parse("1"));
    }
}

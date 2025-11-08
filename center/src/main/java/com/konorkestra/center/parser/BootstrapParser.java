package com.konorkestra.center.parser;

import java.util.*;

import com.konorkestra.center.model.ConfigSet;
import com.konorkestra.center.model.Group;
import com.konorkestra.center.model.Policy;
import com.konorkestra.center.model.SettingFile;
import com.konorkestra.center.parser.exceptions.ParserNotFoundException;
import com.konorkestra.center.parser.exceptions.ParsingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.Yaml; 
/**
 * BootstrapParser
 *
 * Entry point to parse a full Setting file (YAML MVP). Delegates sections to corresponding parsers
 * return a ConfigFile object for persistence as an atomic unit
 * 
 * YAML STRUCTURE -MVP- Groups, Policies and configSets
 * 
 */
@Component
@Slf4j
public class BootstrapParser extends Parser<String, SettingFile>{
    private final Map<String, Parser> parsers;
    private static final Class<SettingFile> PARSED_TYPE = SettingFile.class;
    private static final String HANDLEDKEY = "";
    private final Map<String, java.util.function.BiConsumer<SettingFile, Object>> assigners = new HashMap<>();
    @Autowired
    public BootstrapParser(Collection<Parser> parsers) {
        this.parsers = new HashMap<>();
        for (Parser parser : parsers) {
            this.parsers.put(parser.getHandledKey().toLowerCase(), parser);
        }
        assigners.put("groups", (settingFile, val) -> settingFile.setGroups((List<Group>) val));
        assigners.put("policies", (settingFile, val) -> settingFile.setPolicies((List<Policy>) val));
        assigners.put("configsets", (settingFile, val) -> settingFile.setConfigSets((List<ConfigSet>) val));
        }

    /**
     * 
     * @param yamlContent
     * @return returns the SettingFile object after parsing
     */
    public SettingFile parse(String yamlContent) {
        long start = System.currentTimeMillis();
        SettingFile settingFile = new SettingFile();
        Yaml yaml = new Yaml();
        Object root = yaml.load(yamlContent);
        if (!(root instanceof Map)) {
            throw new ParsingException(
                    "Parsing failed. Root element is not a map."
            );
        }
        @SuppressWarnings("unchecked")
        Map<String, Object> map = (Map<String, Object>) root;
        log.debug("Parsing bootstrap file");
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            Parser parser = parsers.get(entry.getKey().toLowerCase());
            if (parser == null) {
                throw new ParserNotFoundException(
                        "No parser found for key: " + entry.getKey() + "."
                        +" Supported keys: " + parsers.keySet()
                );
            }
            Object parsedSection = parser.parse(entry.getValue());
            java.util.function.BiConsumer<SettingFile, Object> assigner = assigners.get(entry.getKey().toLowerCase());
            if(assigner != null){
                Class<?> expectedType = parser.getParsedType();
                if (!expectedType.isInstance(parsedSection)) {
                    throw new ParsingException(
                            "Parser returned wrong type for key: " + entry.getKey() +
                                    ". Expected: " + expectedType.getSimpleName() +
                                    ", but got: " + parsedSection.getClass().getSimpleName()
                    );
                }
                assigner.accept(settingFile, parsedSection);
            }
            log.debug("Parsed {}", entry.getKey().toLowerCase());
        }
        log.debug("Bootstrap file parsed successfully.");
        log.debug("Parsing took {} ms", System.currentTimeMillis() - start);
        return settingFile;
    }
    
    @Override
    public String getHandledKey() {
        return HANDLEDKEY;
    }
    @Override
    public Class<SettingFile> getParsedType() {
        return PARSED_TYPE;
    }
}
package com.konorkestra.center.parser;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.konorkestra.center.parser.exceptions.ParserNotFoundException;
import com.konorkestra.center.parser.exceptions.ParsingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.Yaml; 
/**
 * BootstrapParser
 *
 * Entry point to parse a full YAML file. Delegates sections to corresponding parsers.
 * 
 * YAML STRUCTURE -MVP-
 *          Groups:x
 *              // group definition and metadata
 *          Policies:
 *              // policy definition and metadata
 *          ConfigSet:
 *              // configSet definition and metadata
 * SEMANTICS ARE ENFORCED BY EACH PARSER THE IDEA ALSO IS TO DECOUPLE THINGS SO MORE COMPLEX THINGS LIKE PolicyParser evolve separately 
 */
@Component
@Slf4j
public class BootstrapParser implements Parser{
    private final Map<String, Parser> parsers;
    private static final String HANDLEDKEY = "bootstrap";
    @Autowired
    public BootstrapParser(Collection<Parser> parsers) {
        this.parsers = new HashMap<>();
        for (Parser parser : parsers) {
            this.parsers.put(parser.getHandledKey().toLowerCase(), parser);
        }
        }

    /**
     * 
     * @param yamlContent
     * @return true on success of parsing the whole content
     */
    public boolean parse(String yamlContent) {
        long start = System.currentTimeMillis();
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
            parser.parse(entry.getValue());
            log.debug("Parsed {}", entry.getKey().toLowerCase());
        }
        log.debug("Bootstrap file parsed successfully.");
        log.debug("Parsing took {} ms", System.currentTimeMillis() - start);
        return true;
    }
    
    @Override
    public String getHandledKey() {
        return HANDLEDKEY;
    }
}
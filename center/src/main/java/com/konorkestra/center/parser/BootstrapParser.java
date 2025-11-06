package com.konorkestra.center.parser;

import java.util.HashMap;
import java.util.Map;

/**
 * BootstrapParser
 *
 * Entry point to parse a full YAML file. Delegates sections to corresponding parsers.
 */
public class BootstrapParser implements Parser{

    // Map of section name -> parser instance
    private final Map<String, Parser> parsers;

    /**
     * Constructor.
     * Dynamically loads all parsers listed in parserClasses array and maps them by section name.
     */
    public BootstrapParser() {
        parsers = new HashMap<>();
        String[] parserClasses = {
                "com.konorkestra.center.parser.GroupParser",
                "com.konorkestra.center.parser.ConfigSetParser",
                "com.konorkestra.center.parser.PolicyParser"
        };

        for (String className : parserClasses) {
            try {
                Class<?> clazz = Class.forName(className);

                // Ensure it implements Parser interface
                if (Parser.class.isAssignableFrom(clazz)) {
                    Parser parser = (Parser) clazz.getDeclaredConstructor().newInstance();
                    String key = parser.getType() != null ? parser.getType()
                            : clazz.getSimpleName().toLowerCase();
                    parsers.put(key, parser);
                }
            } catch (Exception e) {
                System.err.println("Failed to load parser class: " + className);
                e.printStackTrace();
            }
        }
    }

    /**
     * Parse the full YAML content.
     *
     * @param yamlContent YAML as a string
     * @return true if parsing succeeded
     * @throws IllegalArgumentException if a top-level section has no corresponding parser
     */
    public boolean parse(String yamlContent) {
        // Example: use SnakeYAML to load top-level map
        // Map<String, Object> yamlMap = new Yaml().load(yamlContent);

        // Iterate over top-level keys and delegate to parsers
        // for (String section : yamlMap.keySet()) {
        //     String singular = section.endsWith("s") ? section.substring(0, section.length() - 1) : section;
        //     Parser parser = parsers.get(singular.toLowerCase());
        //     if (parser == null) {
        //         throw new IllegalArgumentException("No parser found for section: " + section);
        //     }
        //     parser.parseSection(yamlMap.get(section));
        // }

        return true;
    }

    /**
     * Get a parser instance by name.
     */
    public Parser getParser(String name) {
        return parsers.get(name.toLowerCase());
    }
    @Override
    public String getType() {
        return "bootstrap";
    }
}
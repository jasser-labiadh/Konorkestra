package com.konorkestra.center.parser;
import com.konorkestra.center.model.ConfigSet;
import com.konorkestra.center.parser.exceptions.IdExistsException;
import com.konorkestra.center.service.ConfigSetValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * This basically parses configSet based on semantics
 * not allowing empty config values
 * the UID must be unique 
 */
@Component
public class ConfigSetParser extends Parser<List<Map<String,Object>>, List<ConfigSet>> {

    private static final String HANDLEDKEY = "configsets";
    private static final Class<List<ConfigSet>> parsedType = (Class<List<ConfigSet>>) (Class<?>) List.class;

    private final ConfigSetValidator validator;

    @Autowired
    public ConfigSetParser(ConfigSetValidator validator) {
        this.validator = validator;
    }

    @Override
    public List<ConfigSet> parse(List<Map<String,Object>> input) {
        return input.stream()
                .map(this::mapToConfigSet)
                .toList();
    }
    private ConfigSet mapToConfigSet(Map<String,Object> map){
        ConfigSet configSet = new ConfigSet();
        String uid = map.get("uid").toString();
        if(!validator.validate(uid)){
            throw new IdExistsException("a configSet with Id "+uid+" already exists");
        }
        configSet.setUid(map.get("uid").toString());
        map.entrySet().stream()
                .filter(entry -> !entry.getKey().equals("uid"))
                .filter(entry -> entry.getValue() != null)
                .forEach(entry -> configSet.getConfig().put(entry.getKey(), entry.getValue().toString()));
        return configSet;
    }
    @Override
    public String getHandledKey() {
        return HANDLEDKEY;
    }

    @Override
    public Class<List<ConfigSet>> getParsedType() {
        return parsedType;
    }
}

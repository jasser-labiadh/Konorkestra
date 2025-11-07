package com.konorkestra.center.parser;
import com.konorkestra.center.model.ConfigSet;
import com.konorkestra.center.service.ConfigSetValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.Yaml;
import java.util.Map;
import java.util.Map.Entry;

/**
 * This basically parses configSet based on semantics
 * not allowing empty config values
 * the UID must be unique
 */
@Component
public class ConfigSetParser implements Parser {
    
    private static final String HANDLEDKEY = "configsets";
    private ConfigSetValidator validator;
    @Autowired
    public ConfigSetParser(ConfigSetValidator validator) {
        validator = new ConfigSetValidator();
    }
    /**
     * 
     * @param obj the yaml parsed object
     * @return true if parsing is semantically correct
     */
    public boolean parse(Object obj) {
        Yaml yaml = new Yaml();
        ConfigSet configSet = yaml.loadAs(yaml.dump(obj), ConfigSet.class);
        return configSet != null && configSet.getUid() != null && checkNonNull(configSet.getConfig()) && validator.validate();
    }
    private boolean checkNonNull(Map<String,String> config){
        for(Entry<String,String> entry : config.entrySet()){
            if(entry.getValue() == null){
                return false;
            }
        }
        return true;
    }
    
    public String getHandledKey(){
        return HANDLEDKEY;
    }
}

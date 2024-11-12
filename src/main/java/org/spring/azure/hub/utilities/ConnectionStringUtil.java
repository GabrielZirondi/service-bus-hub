package org.spring.azure.hub.utilities;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

@Slf4j @UtilityClass
public class ConnectionStringUtil {

    private static final String ENTITY_PATH = "EntityPath=";
    private static final String END_STRING = ";";

    public static String ensureEntityPathInConnectionString(String entityConnectionString,
                                                            String name,
                                                            String sbsConnectionString) {
        if (!StringUtils.hasText(entityConnectionString)) {
            log.debug("Entity connection string is missing or empty. Falling back to the default connection string.");
            entityConnectionString = sbsConnectionString;
        }
        if (!entityConnectionString.contains(ENTITY_PATH)) {
            log.trace("Entity path not found in connection string. Appending entity path for '{}'", name);
            if (!entityConnectionString.endsWith(END_STRING))
                entityConnectionString += END_STRING;
            entityConnectionString += ENTITY_PATH + name;
        }
        return entityConnectionString;
    }
}
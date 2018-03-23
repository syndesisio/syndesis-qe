package io.syndesis.qe.accounts;

import java.util.Map;

import lombok.Data;
import lombok.Getter;

/**
 * Contains information about a third party service account.
 *
 * @author jknetl
 */
@Data
public class Account {

    @Getter
    private String service;
    private Map<String, String> properties;

    public String getProperty(String name) {
        return properties.get(name);
    }
}

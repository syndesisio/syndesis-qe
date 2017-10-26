package io.syndesis.qe.rest.accounts;

import java.util.Map;

import lombok.Data;

/**
 * Contains information about a third party service account.
 * @author jknetl
 */
@Data
public class Account {

	private String service;
	private Map<String, String> properties;


	public String getProperty(String name) {
		return properties.get(name);
	}
}

package io.syndesis.qe.utils;

import org.json.JSONArray;
import org.json.JSONObject;

import lombok.extern.slf4j.Slf4j;

/**
 * Used to generate filter rules: "[{ \"path\": \"text\", \"op\": \"contains\", \"value\": \"#backendTest\" }]"
 *
 * Nov 6, 2017 Red Hat
 *
 * @author tplevko@redhat.com
 */
@Slf4j
public class FilterRulesBuilder {

	private final JSONArray myArray;
	private final JSONObject json;

	public FilterRulesBuilder() {
		myArray = new JSONArray();
		json = new JSONObject();
	}

	public FilterRulesBuilder addPath(String path) {
		return addField("path", path);
	}

	public FilterRulesBuilder addOps(String op) {
		return addField("op", op);
	}

	public FilterRulesBuilder addValue(String value) {
		return addField("value", value);
	}

	private FilterRulesBuilder addField(String name, String value) {
		json.put(name, value);
		return this;
	}

	public String build() {
		myArray.put(json);
		return myArray.toString();
	}
}

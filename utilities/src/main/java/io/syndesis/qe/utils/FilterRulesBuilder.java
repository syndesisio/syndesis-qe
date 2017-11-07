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

	private JSONArray myArray;

	public FilterRulesBuilder() {
		myArray = new JSONArray();
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
		final JSONObject json = new JSONObject();
		json.put(name, value);
		myArray.put(json);
		return this;
	}

	public String build() {
		return myArray.toString();
	}
}

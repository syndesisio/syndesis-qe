package io.syndesis.qe.rest.utils;

import org.apache.commons.io.IOUtils;

import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;

import lombok.extern.slf4j.Slf4j;

/**
 * JSON utils used for testing.
 *
 * Jun 21, 2017 Red Hat
 *
 * @author tplevko@redhat.com
 */
@Slf4j
public final class JsonUtils {

	private JsonUtils() {
	}

	/**
	 * There are sample json files in resources, this is used for reading of them and loading into JSONObjects.
	 *
	 * @param fileName
	 * @return
	 */
	public static JSONObject readJsonFromFile(String fileName) {

		JSONObject json = null;
		final ClassLoader classloader = Thread.currentThread().getContextClassLoader();
		final InputStream is = classloader.getResourceAsStream(fileName);

		String jsonTxt = null;
		try {
			jsonTxt = IOUtils.toString(is, "UTF-8");
		} catch (IOException ex) {
			log.error("Could not read the json file from: " + fileName, ex.toString());
		}

		json = new JSONObject(jsonTxt);

		return json;
	}
}

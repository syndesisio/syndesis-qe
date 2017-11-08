package io.syndesis.qe.configuration;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.Properties;

public final class PropertiesLoader {

	public PropertiesLoader() {
	}

	private Properties properties = new Properties();

	public void load(String absolutePath) {
		InputStream input = null;
		final String defaultFileName = "default.properties";

		try {
			if (absolutePath == null) {
				input = new FileInputStream(Thread.currentThread().getContextClassLoader().getResource(defaultFileName).toURI().getPath());
			} else {
				input = new FileInputStream(absolutePath);
			}

			// load a io.syndesis.qe.configuration file
			properties.load(input);
		} catch (FileNotFoundException ex) {
			ex.printStackTrace();
		} catch (IOException ex) {
			ex.printStackTrace();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public String get(String key) {
		final String value = properties.getProperty(key);

		if (value == null) {
			return "";
		}

		return value;
	}
}

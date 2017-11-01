package properties;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.Properties;

/**
 * Created by mastepan on 10/30/17.
 */
public final class PropertiesLoader {

	private PropertiesLoader() {
	}

	private static Properties properties = new Properties();

	public static void load(String absolutePath) {
		InputStream input = null;
		final String defaultFileName = "default.properties";

		try {
			if (absolutePath == null) {
				input = new FileInputStream(Thread.currentThread().getContextClassLoader().getResource(defaultFileName).toURI().getPath());
			} else {
				input = new FileInputStream(absolutePath);
			}

			// load a properties file
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

	public static String get(String key) {
		final String value = properties.getProperty(key);

		if (value == null) {
			return "";
		}

		return value;
	}
}

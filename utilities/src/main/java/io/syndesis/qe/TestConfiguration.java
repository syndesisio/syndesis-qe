package io.syndesis.qe;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TestConfiguration {

	public static final String OPENSHIFT_URL = "syndesis.config.openshift.url";
	public static final String OPENSHIFT_TOKEN = "syndesis.config.openshift.token";
	public static final String OPENSHIFT_NAMESPACE = "syndesis.config.openshift.namespace";


	private static final TestConfiguration INSTANCE = new TestConfiguration();

	private final Properties properties = new Properties();

	private TestConfiguration() {
		// first let's try product properties
		copyValues(fromPath("test.properties"), true);

		// then product properties
		copyValues(fromPath("../test.properties"));

		// then system variables
		copyValues(System.getProperties());

		// then environment variables
		// TODO: copyValues(fromEnvironment());
    	
		// then defaults
		copyValues(defaultValues());
	}

	public static TestConfiguration get() {
		return INSTANCE;
	}

	public static String openShiftUrl() {
		return get().readValue(OPENSHIFT_URL);
	}

	public static String openShiftToken() { return get().readValue(OPENSHIFT_TOKEN); }

	public static String openShiftNamespace() { return get().readValue(OPENSHIFT_NAMESPACE); }


	private Properties defaultValues() {
		final Properties props = new Properties();

		props.setProperty(OPENSHIFT_URL, "");
		props.setProperty(OPENSHIFT_TOKEN, "");

		return props;
	}

	public String readValue(final String key) {
		return readValue(key, null);
	}

	public String readValue(final String key, final String defaultValue) {
		return this.properties.getProperty(key, defaultValue);
	}

	private Properties fromPath(final String path) {
		final Properties props = new Properties();

		final Path propsPath = Paths.get(path)
				.toAbsolutePath();
		if (Files.isReadable(propsPath)) {
			try (InputStream is = Files.newInputStream(propsPath)) {
				props.load(is);
			} catch (final IOException ex) {
				log.warn("Unable to read properties from '{}'", propsPath);
				log.debug("Exception", ex);
			}
		}

		return props;
	}

	private void copyValues(final Properties source) {
		copyValues(source, false);
	}

	private void copyValues(final Properties source, final boolean overwrite) {
		source.stringPropertyNames().stream()
				.filter(key -> overwrite || !this.properties.containsKey(key))
				.forEach(key -> this.properties.setProperty(key, source.getProperty(key)));
	}

}

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
	public static final String OPENSHIFT_ROUTE_SUFFIX = "syndesis.config.openshift.route.suffix";

	public static final String SYNDESIS_UI_USERNAME = "syndesis.config.ui.username";
	public static final String SYNDESIS_UI_PASSWORD = "syndesis.config.ui.password";
	public static final String SYNDESIS_UI_URL = "syndesis.config.ui.url";
	public static final String SYNDESIS_UI_BROWSER = "syndesis.config.ui.browser";

	public static final String SYNDESIS_REST_URL = "syndesis.config.rest.url";
	public static final String SYNDESIS_REST_API_PATH = "syndesis.config.rest.api.path";

	public static final String SYNDESIS_CREDENTIALS_FILE = "syndesis.config.credentials.file";
	public static final String SYNDESIS_VERSIONS_FILE = "syndesis.config.versions.file";

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

	public static String openShiftRouteSuffix() { return get().readValue(OPENSHIFT_ROUTE_SUFFIX); }

	public static String syndesisUsername() { return get().readValue(SYNDESIS_UI_USERNAME); }

	public static String syndesisPassword() { return get().readValue(SYNDESIS_UI_PASSWORD); }

	public static String syndesisUrl() { return get().readValue(SYNDESIS_UI_URL); }

	public static String syndesisRestUrl() { return get().readValue(SYNDESIS_REST_URL); }

	public static String syndesisRestApiPath() { return get().readValue(SYNDESIS_REST_API_PATH); }

	public static String syndesisCredentialsFile() { return get().readValue(SYNDESIS_CREDENTIALS_FILE); }

	public static String syndesisVersionsFile() { return get().readValue(SYNDESIS_VERSIONS_FILE); }

	private Properties defaultValues() {
		final Properties props = new Properties();

		props.setProperty(OPENSHIFT_URL, "");
		props.setProperty(OPENSHIFT_TOKEN, "");
		props.setProperty(SYNDESIS_REST_API_PATH, "/api/v1");

		props.setProperty(SYNDESIS_CREDENTIALS_FILE, "../credentials.json");
		props.setProperty(SYNDESIS_VERSIONS_FILE, "src/test/resources/dependencyVersions.properties");

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

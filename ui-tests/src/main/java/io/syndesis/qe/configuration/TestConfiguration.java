package io.syndesis.qe.configuration;

public final class TestConfiguration {

	private static final String SYNDESIS_LOGIN_URL = "syndesis.login.url";
	private static final String SYNDESIS_LOGIN_USERNAME = "syndesis.login.username";
	private static final String SYNDESIS_LOGIN_PASSWORD = "syndesis.login.password";

	private static PropertiesLoader properties = new PropertiesLoader();

	private TestConfiguration() {
	}

	public static void loadFromFile(String absolutePath) {
		properties.load(absolutePath);
	}

	public static String getSyndesisLoginUrl() {
		return properties.get(SYNDESIS_LOGIN_URL);
	}
	public static String getSyndesisLoginUsername() {
		return properties.get(SYNDESIS_LOGIN_USERNAME);
	}
	public static String getSyndesisLoginPassword() {
		return properties.get(SYNDESIS_LOGIN_PASSWORD);
	}
}

package io.syndesis.qe;

import io.syndesis.qe.utils.TestUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.Properties;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TestConfiguration {

    private static final String TEST_PROPERTIES_FILE = "syndesis.config.test.properties";

    public static final String OPENSHIFT_URL = "syndesis.config.openshift.url";
    public static final String OPENSHIFT_TOKEN = "syndesis.config.openshift.token";
    public static final String OPENSHIFT_NAMESPACE = "syndesis.config.openshift.namespace";
    public static final String OPENSHIFT_SAR_NAMESPACE = "syndesis.config.openshift.sar_namespace";
    public static final String OPENSHIFT_NAMESPACE_CLEANUP = "syndesis.config.openshift.namespace.cleanup";
    public static final String OPENSHIFT_NAMESPACE_CLEANUP_AFTER = "syndesis.config.openshift.namespace.cleanup.after";
    public static final String OPENSHIFT_ROUTE_SUFFIX = "syndesis.config.openshift.route.suffix";
    public static final String OPENSHIFT_NAMESPACE_LOCK = "syndesis.config.openshift.namespace.lock";

    public static final String SYNDESIS_UI_USERNAME = "syndesis.config.ui.username";
    public static final String SYNDESIS_UI_PASSWORD = "syndesis.config.ui.password";
    public static final String SYNDESIS_UI_URL = "syndesis.config.ui.url";
    public static final String SYNDESIS_UI_BROWSER = "syndesis.config.ui.browser";
    public static final String SYNDESIS_URL_SUFFIX = "syndesis.config.url.suffix";
    public static final String SYNDESIS_CALLBACK_URL_SUFFIX = "syndesis.config.callbackUrlSuffix";
    public static final String SYNDESIS_ENABLE_TEST_SUPPORT = "syndesis.config.enableTestSupport";

    public static final String SYNDESIS_REST_API_PATH = "syndesis.config.rest.api.path";
    public static final String SYNDESIS_SERVER_ROUTE = "syndesis.config.server.route";

    public static final String SYNDESIS_CREDENTIALS_FILE = "syndesis.config.credentials.file";
    public static final String SYNDESIS_VERSIONS_FILE = "syndesis.config.versions.file";

    public static final String SYNDESIS_INSTALL_VERSION = "syndesis.config.install.version";
    public static final String SYNDESIS_CRD_URL = "syndesis.config.crd.url";
    public static final String SYNDESIS_OPERATOR_IMAGE = "syndesis.config.operator.image";
    public static final String SYNDESIS_CR_URL = "syndesis.config.cr.url";

    public static final String SYNDESIS_PULL_SECRET = "syndesis.config.pull.secret";
    public static final String SYNDESIS_PULL_SECRET_NAME = "syndesis.config.pull.secret.name";

    public static final String SYNDESIS_PUBLIC_OAUTH_PROXY_URL = "syndesis.config.template.public.oath.proxy.url";

    public static final String SYNDESIS_CUSTOM_RESOURCE_PLURAL = "syndesis.config.custom.resource.plural";

    public static final String JENKINS_DELAY = "jenkins.delay";
    public static final String TESTSUITE_TIMEOUT = "syndesis.config.timeout";

    public static final String DB_ALLOCATOR_URL = "syndesis.dballocator.url";

    public static final String PROD_REPOSITORY = "syndesis.config.prod.repository";
    public static final String UPSTREAM_REPOSITORY = "syndesis.config.upstream.repository";

    public static final String SKIP_TESTS_WITH_OPEN_ISSUES = "syndesis.skip.open.issues";

    public static final String STATE_CHECK_INTERVAL = "syndesis.server.state.check.interval";

    private static final String BROWSER_BINARY_PATH = "syndesis.config.browser.path";

    private static final TestConfiguration INSTANCE = new TestConfiguration();

    private final Properties properties = new Properties();

    private TestConfiguration() {
        // first let's try properties in module dir
        copyValues(fromPath("test.properties"), true);

        // then properties in repo root
        copyValues(fromPath("../" + System.getProperty(TEST_PROPERTIES_FILE, "test.properties")));

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

    public static String openShiftToken() {
        return get().readValue(OPENSHIFT_TOKEN);
    }

    public static String openShiftNamespace() {
        return get().readValue(OPENSHIFT_NAMESPACE);
    }

    // Namespace for the subject access review. If unspecified, will use the same namespace as for the deployment.
    public static String openShiftSARNamespace() {
        return get().readValue(OPENSHIFT_SAR_NAMESPACE, get().readValue(OPENSHIFT_NAMESPACE));
    }

    public static String openShiftRouteSuffix() {
        return get().readValue(OPENSHIFT_ROUTE_SUFFIX);
    }

    public static String syndesisUsername() {
        return get().readValue(SYNDESIS_UI_USERNAME);
    }

    public static String syndesisPassword() {
        return get().readValue(SYNDESIS_UI_PASSWORD);
    }

    public static String syndesisUrl() {
        return get().readValue(SYNDESIS_UI_URL);
    }

    public static String syndesisCallbackUrlSuffix() {
        return get().readValue(SYNDESIS_CALLBACK_URL_SUFFIX);
    }

    public static String syndesisRestApiPath() {
        return get().readValue(SYNDESIS_REST_API_PATH);
    }

    public static String syndesisCredentialsFile() {
        return get().readValue(SYNDESIS_CREDENTIALS_FILE);
    }

    public static String syndesisVersionsFile() {
        return get().readValue(SYNDESIS_VERSIONS_FILE);
    }

    public static String syndesisPublicOauthProxyTemplateUrl() {
        return get().readValue(SYNDESIS_PUBLIC_OAUTH_PROXY_URL);
    }

    public static String syndesisPullSecret() {
        return get().readValue(SYNDESIS_PULL_SECRET);
    }

    public static String syndesisPullSecretName() {
        return get().readValue(SYNDESIS_PULL_SECRET_NAME);
    }

    public static boolean enableTestSupport() {
        return Boolean.parseBoolean(get().readValue(SYNDESIS_ENABLE_TEST_SUPPORT, "false"));
    }

    public static String syndesisCrdUrl() {
        return get().readValue(SYNDESIS_CRD_URL);
    }
    public static String syndesisOperatorImage() {
        return get().readValue(SYNDESIS_OPERATOR_IMAGE);
    }

    public static String syndesisCrUrl() {
        return get().readValue(SYNDESIS_CR_URL);
    }

    public static String syndesisBrowser() {
        return get().readValue(SYNDESIS_UI_BROWSER);
    }

    public static boolean namespaceCleanup() {
        return Boolean.parseBoolean(get().readValue(OPENSHIFT_NAMESPACE_CLEANUP));
    }

    public static boolean namespaceCleanupAfter() {
        return Boolean.parseBoolean(get().readValue(OPENSHIFT_NAMESPACE_CLEANUP_AFTER));
    }

    public static boolean namespaceLock() {
        return Boolean.parseBoolean(get().readValue(OPENSHIFT_NAMESPACE_LOCK));
    }

    public static boolean useServerRoute() {
        return Boolean.parseBoolean(get().readValue(SYNDESIS_SERVER_ROUTE));
    }

    public static String customResourcePlural() {
        return get().readValue(SYNDESIS_CUSTOM_RESOURCE_PLURAL);
    }

    public static int getJenkinsDelay() {
        return Integer.parseInt(get().readValue(JENKINS_DELAY, "1"));
    }

    public static int getConfigTimeout() {
        return Integer.parseInt(get().readValue(TESTSUITE_TIMEOUT, "30"));
    }

    public static String getDbAllocatorUrl() {
        return get().readValue(DB_ALLOCATOR_URL, "localhost:8080");
    }

    public static String prodRepository() {
        return get().readValue(PROD_REPOSITORY);
    }

    public static String upstreamRepository() {
        return get().readValue(UPSTREAM_REPOSITORY);
    }

    public static Boolean skipTestsWithOpenIssues() {
        return Boolean.parseBoolean(get().readValue(SKIP_TESTS_WITH_OPEN_ISSUES));
    }

    public static int stateCheckInterval() {
        return Integer.parseInt(get().readValue(STATE_CHECK_INTERVAL));
    }

    private Properties defaultValues() {
        final Properties props = new Properties();

        props.setProperty(OPENSHIFT_URL, "");
        props.setProperty(OPENSHIFT_TOKEN, "");
        props.setProperty(SYNDESIS_REST_API_PATH, "/api/v1");
        props.setProperty(SYNDESIS_SERVER_ROUTE, "false");

        props.setProperty(SYNDESIS_CREDENTIALS_FILE, "../credentials.json");
        props.setProperty(SYNDESIS_VERSIONS_FILE, "src/test/resources/dependencyVersions.properties");

        props.setProperty(SYNDESIS_UI_BROWSER, "chrome");

        props.setProperty(OPENSHIFT_NAMESPACE_CLEANUP, "false");
        props.setProperty(OPENSHIFT_NAMESPACE_CLEANUP_AFTER, props.getProperty(OPENSHIFT_NAMESPACE_CLEANUP));
        props.setProperty(OPENSHIFT_NAMESPACE_LOCK, "false");

        // to keep backward compatibility
        if (props.getProperty(SYNDESIS_URL_SUFFIX) != null && props.getProperty(OPENSHIFT_ROUTE_SUFFIX) == null) {
            props.setProperty(OPENSHIFT_ROUTE_SUFFIX, props.getProperty(SYNDESIS_URL_SUFFIX));
        }

        String syndesisVersion;
        if (System.getProperty(SYNDESIS_INSTALL_VERSION) != null) {
            syndesisVersion = System.getProperty(SYNDESIS_INSTALL_VERSION);
        } else {
            syndesisVersion = "master";
            // only use defined system property if it doesnt end with SNAPSHOT and it is not prod build
            if (!System.getProperty("syndesis.version").endsWith("SNAPSHOT") && !TestUtils.isProdBuild()) {
                syndesisVersion = System.getProperty("syndesis.version");
            }
        }

        if (props.getProperty(SYNDESIS_PUBLIC_OAUTH_PROXY_URL) == null) {
            props.setProperty(SYNDESIS_PUBLIC_OAUTH_PROXY_URL, String
                .format("https://raw.githubusercontent.com/syndesisio/syndesis/%s/install/support/syndesis-public-oauth-proxy.yml", syndesisVersion));
        }

        props.setProperty(SYNDESIS_CRD_URL,
            String.format("https://raw.githubusercontent.com/syndesisio/syndesis/%s/install/operator/deploy/syndesis-crd.yml", syndesisVersion));
        String operatorVersion;
        if (this.properties.getProperty(SYNDESIS_INSTALL_VERSION) != null) {
            operatorVersion = this.properties.getProperty(SYNDESIS_INSTALL_VERSION);
        } else {
            operatorVersion = "latest";
        }
        props.setProperty(SYNDESIS_OPERATOR_IMAGE, "syndesis/syndesis-operator" + ':' + operatorVersion);
        props.setProperty(SYNDESIS_CR_URL, getClass().getClassLoader().getResource("syndesis-minimal.yaml").toString());

        props.setProperty(SYNDESIS_CUSTOM_RESOURCE_PLURAL, "syndesises");

        // Copy syndesis properties to their xtf counterparts - used by binary oc client
        System.setProperty("xtf.openshift.url", properties.getProperty(OPENSHIFT_URL));
        System.setProperty("xtf.openshift.master.username", properties.getProperty(SYNDESIS_UI_USERNAME));
        System.setProperty("xtf.openshift.master.password", properties.getProperty(SYNDESIS_UI_PASSWORD));
        System.setProperty("xtf.openshift.namespace", properties.getProperty(OPENSHIFT_NAMESPACE));

        // Set oc version - this version of the client will be used as the binary client
        System.setProperty("xtf.openshift.version", "3.10.70");

        if (props.getProperty(SYNDESIS_PULL_SECRET_NAME) == null) {
            props.setProperty(SYNDESIS_PULL_SECRET_NAME, "syndesis-pull-secret");
        }

        props.setProperty(STATE_CHECK_INTERVAL, "" + (TestUtils.isJenkins() ? 150 : 60));
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

    public static Optional<String> browserBinary() {
        return Optional.ofNullable(get().readValue(BROWSER_BINARY_PATH));
    }
}

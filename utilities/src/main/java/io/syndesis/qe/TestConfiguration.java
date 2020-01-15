package io.syndesis.qe;

import io.syndesis.qe.utils.TestUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.Properties;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TestConfiguration {

    private static final String TEST_PROPERTIES_FILE = "syndesis.config.test.properties";

    public static final String SYNDESIS_VERSION = "syndesis.version";

    public static final String OPENSHIFT_URL = "syndesis.config.openshift.url";
    public static final String OPENSHIFT_TOKEN = "syndesis.config.openshift.token";
    public static final String OPENSHIFT_NAMESPACE = "syndesis.config.openshift.namespace";
    public static final String OPENSHIFT_SAR_NAMESPACE = "syndesis.config.openshift.sar_namespace";
    public static final String OPENSHIFT_NAMESPACE_CLEANUP = "syndesis.config.openshift.namespace.cleanup";
    public static final String OPENSHIFT_ROUTE_SUFFIX = "syndesis.config.openshift.route.suffix";

    public static final String SYNDESIS_SINGLE_USER = "syndesis.config.single.user";
    public static final String SYNDESIS_ADMIN_USERNAME = "syndesis.config.admin.username";
    public static final String SYNDESIS_ADMIN_PASSWORD = "syndesis.config.admin.password";
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
    public static final String SYNDESIS_BUILD_PROPERTIES_URL = "syndesis.config.build.properties.url";

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
    public static final String SNOOP_SELECTORS = "syndesis.config.snoop.selectors";

    private static final String BROWSER_BINARY_PATH = "syndesis.config.browser.path";

    private static final TestConfiguration INSTANCE = new TestConfiguration();

    private final Properties properties = new Properties();

    private TestConfiguration() {
        try {
            // first let's try properties in module dir
            copyValues(fromPath("test.properties"), true);

            // then properties in repo root
            copyValues(fromPath("../" + System.getProperty(TEST_PROPERTIES_FILE, "test.properties")));

            // build properties file
            copyValues(fromPath(System.getProperty(SYNDESIS_BUILD_PROPERTIES_URL)));

            // then system variables
            copyValues(System.getProperties());

            // then environment variables
            // TODO: copyValues(fromEnvironment());

            // then defaults
            copyValues(defaultValues());
        } catch (Exception e) {
            // If it fails here, then the user doesn't get any meaningful error other than:
            // java.lang.NoClassDefFoundError: Could not initialize class io.syndesis.qe.TestConfiguration
            log.error(
                "Error while creating TestConfiguration class! Please check if all mandatory properties are set" +
                    " (either in test.properties or via the command line)");
            e.printStackTrace();
            throw e;
        }
    }

    public static TestConfiguration get() {
        return INSTANCE;
    }

    public static String syndesisVersion() {
        return get().readValue(SYNDESIS_VERSION);
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

    public static String adminUsername() {
        return get().readValue(SYNDESIS_ADMIN_USERNAME);
    }

    public static String adminPassword() {
        return get().readValue(SYNDESIS_ADMIN_PASSWORD);
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

    public static boolean snoopSelectors() {
        return Boolean.parseBoolean(get().readValue(SNOOP_SELECTORS));
    }

    public static String image(Image image) {
        return get().readValue(image.name());
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
            if (!properties.getProperty(SYNDESIS_VERSION).endsWith("SNAPSHOT") && !properties.getProperty(SYNDESIS_VERSION).contains("redhat")) {
                syndesisVersion = System.getProperty(SYNDESIS_VERSION);
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

        // When the single user property is set (for the env where the syndesis is already deployed and you are not an admin)
        // Make the user "admin" anyway, as that user is used in all k8s client invocations by default
        if (properties.getProperty(SYNDESIS_SINGLE_USER) != null) {
            props.setProperty(SYNDESIS_ADMIN_USERNAME, properties.getProperty(SYNDESIS_UI_USERNAME));
        }
        if (properties.getProperty(SYNDESIS_SINGLE_USER) != null) {
            props.setProperty(SYNDESIS_ADMIN_PASSWORD, properties.getProperty(SYNDESIS_UI_PASSWORD));
        }

        // Copy syndesis properties to their xtf counterparts - used by binary oc client
        System.setProperty("xtf.openshift.url", properties.getProperty(OPENSHIFT_URL));
        System.setProperty("xtf.openshift.master.username", properties.getProperty(SYNDESIS_ADMIN_USERNAME));
        System.setProperty("xtf.openshift.master.password", properties.getProperty(SYNDESIS_ADMIN_PASSWORD));
        System.setProperty("xtf.openshift.namespace", properties.getProperty(OPENSHIFT_NAMESPACE));

        // Set oc version - this version of the client will be used as the binary client
        System.setProperty("xtf.openshift.version", "3.10.70");

        if (props.getProperty(SYNDESIS_PULL_SECRET_NAME) == null) {
            props.setProperty(SYNDESIS_PULL_SECRET_NAME, "syndesis-pull-secret");
        }

        props.setProperty(STATE_CHECK_INTERVAL, "" + (TestUtils.isJenkins() ? 150 : 60));
        if (props.getProperty(SNOOP_SELECTORS) == null) {
            props.setProperty(SNOOP_SELECTORS, "false");
        }
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

        if (path == null) {
            return props;
        }

        final Path propsPath = Paths.get(path).toAbsolutePath();
        InputStream is = null;
        try {
            if (path.startsWith("http")) {
                is = new URL(path).openStream();
            } else if (Files.isReadable(propsPath)) {
                is = Files.newInputStream(propsPath);
            }

            if (is != null) {
                log.debug("Loading properties from " + path);
                props.load(is);
                is.close();
            }
        } catch (IOException e) {
            log.warn("Unable to read properties from " + propsPath, e);
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

    /**
     * Used in upgrade test to specify different operator image to use.
     *
     * @param img image to use
     */
    public void overrideSyndesisOperatorImage(String img) {
        properties.setProperty(SYNDESIS_OPERATOR_IMAGE, img);
    }

    /**
     * Temporary override for the CR URL used in upgrade tests, needed because of the change from
     * enabled: "true" to enabled: true in the operator.
     */
    public void overrideSyndesisCrUrl() {
        properties.setProperty(SYNDESIS_CR_URL, getClass().getClassLoader().getResource("syndesis-minimal-1.8.x.yaml").toString());
    }
}

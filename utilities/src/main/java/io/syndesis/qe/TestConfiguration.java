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

    public static final String SYNDESIS_REST_API_PATH = "syndesis.config.rest.api.path";
    public static final String SYNDESIS_SERVER_ROUTE = "syndesis.config.server.route";

    public static final String SYNDESIS_CREDENTIALS_FILE = "syndesis.config.credentials.file";
    public static final String SYNDESIS_VERSIONS_FILE = "syndesis.config.versions.file";

    public static final String SYNDESIS_TEMPLATE_URL = "syndesis.config.template.url";
    public static final String SYNDESIS_TEMPLATE_SA = "syndesis.config.template.sa";
    public static final String SYNDESIS_TEMPLATE_VERSION = "syndesis.config.template.version";
    public static final String SYNDESIS_TEMPLATE_USE_OPERATOR = "syndesis.config.template.use.operator";
    public static final String SYNDESIS_OPERATOR_CRD_URL = "syndesis.config.operator.crd";
    public static final String SYNDESIS_OPERATOR_URL = "syndesis.config.operator.url";
    public static final String SYNDESIS_OPERATOR_TEMPLATE_URL = "syndesis.config.operator.template.url";

    public static final String JENKINS_DELAY = "jenkins.delay";
    public static final String TESTSUITE_TIMEOUT = "syndesis.config.timeout";

    public static final String DB_ALLOCATOR_URL = "syndesis.dballocator.url";

    public static final String PROD_REPOSITORY = "syndesis.config.prod.repository";
    public static final String UPSTREAM_REPOSITORY = "syndesis.config.upstream.repository";

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

    // Namespace for the subject access review. If unspecified, will use the same namespace as for the deployment.
    public static String openShiftSARNamespace() { return get().readValue(OPENSHIFT_SAR_NAMESPACE, get().readValue(OPENSHIFT_NAMESPACE)); }

    public static String openShiftRouteSuffix() { return get().readValue(OPENSHIFT_ROUTE_SUFFIX); }

    public static String syndesisUsername() { return get().readValue(SYNDESIS_UI_USERNAME); }

    public static String syndesisPassword() { return get().readValue(SYNDESIS_UI_PASSWORD); }

    public static String syndesisUrl() { return get().readValue(SYNDESIS_UI_URL); }

    public static String syndesisCallbackUrlSuffix() { return get().readValue(SYNDESIS_CALLBACK_URL_SUFFIX); }

    public static String syndesisRestApiPath() { return get().readValue(SYNDESIS_REST_API_PATH); }

    public static String syndesisCredentialsFile() { return get().readValue(SYNDESIS_CREDENTIALS_FILE); }

    public static String syndesisVersionsFile() { return get().readValue(SYNDESIS_VERSIONS_FILE); }

    public static String syndesisTemplateUrl() { return get().readValue(SYNDESIS_TEMPLATE_URL); }

    public static String syndesisTemplateSA() { return get().readValue(SYNDESIS_TEMPLATE_SA); }

    public static boolean useOperator() {
        return Boolean.parseBoolean(get().readValue(SYNDESIS_TEMPLATE_USE_OPERATOR));
    }

    public static String syndesisOperatorCrdUrl() {
        return get().readValue(SYNDESIS_OPERATOR_CRD_URL);
    }

    public static String syndesisOperatorUrl() {
        return get().readValue(SYNDESIS_OPERATOR_URL);
    }

    public static String syndesisOperatorTemplateUrl() {
        return get().readValue(SYNDESIS_OPERATOR_TEMPLATE_URL);
    }

    public static String syndesisBrowser() { return get().readValue(SYNDESIS_UI_BROWSER); }

    public static boolean namespaceCleanup() { return Boolean.parseBoolean(get().readValue(OPENSHIFT_NAMESPACE_CLEANUP)); }

    public static boolean namespaceCleanupAfter() { return Boolean.parseBoolean(get().readValue(OPENSHIFT_NAMESPACE_CLEANUP_AFTER)); }

    public static boolean namespaceLock() { return Boolean.parseBoolean(get().readValue(OPENSHIFT_NAMESPACE_LOCK)); }

    public static boolean useServerRoute() { return Boolean.parseBoolean(get().readValue(SYNDESIS_SERVER_ROUTE)); }

    public static int getJenkinsDelay() { return Integer.parseInt(get().readValue(JENKINS_DELAY, "1")); }

    public static int getConfigTimeout() {return Integer.parseInt(get().readValue(TESTSUITE_TIMEOUT, "300")); }

    public static String getDbAllocatorUrl(){ return get().readValue(DB_ALLOCATOR_URL, "localhost:8080"); }

    public static String prodRepository() {
        return get().readValue(PROD_REPOSITORY);
    }

    public static String upstreamRepository() {
        return get().readValue(UPSTREAM_REPOSITORY);
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
        if (System.getProperty(SYNDESIS_TEMPLATE_VERSION) != null) {
            syndesisVersion = System.getProperty(SYNDESIS_TEMPLATE_VERSION);
        } else {
            syndesisVersion = "master";
            // only use defined system property if it doesnt end with SNAPSHOT and it is not prod build
            if (!System.getProperty("syndesis.version").endsWith("SNAPSHOT") && !System.getProperty("syndesis.version").contains("redhat")) {
                syndesisVersion = System.getProperty("syndesis.version");
            }
        }

        if (props.getProperty(SYNDESIS_TEMPLATE_URL) == null) {
            props.setProperty(SYNDESIS_TEMPLATE_URL, String.format("https://raw.githubusercontent.com/syndesisio/syndesis/%s/install/syndesis.yml", syndesisVersion));
        }
        if (props.getProperty(SYNDESIS_OPERATOR_URL) == null) {
            props.setProperty(SYNDESIS_OPERATOR_URL, String.format("https://raw.githubusercontent.com/syndesisio/syndesis/%s/install/operator/deploy/syndesis-operator.yml", syndesisVersion));
        }

        props.setProperty(SYNDESIS_OPERATOR_CRD_URL, String.format("https://raw.githubusercontent.com/syndesisio/syndesis/%s/install/operator/deploy/syndesis-crd.yml", syndesisVersion));
        props.setProperty(SYNDESIS_OPERATOR_TEMPLATE_URL, String.format("https://raw.githubusercontent.com/syndesisio/syndesis/%s/install/operator/deploy/syndesis.yml", syndesisVersion));

        props.setProperty(SYNDESIS_TEMPLATE_USE_OPERATOR, "true");

        // Copy syndesis properties to their xtf counterparts - used by binary oc client
        System.setProperty("xtf.config.master.url", properties.getProperty(OPENSHIFT_URL));
        System.setProperty("xtf.config.master.username", properties.getProperty(SYNDESIS_UI_USERNAME));
        System.setProperty("xtf.config.master.password", properties.getProperty(SYNDESIS_UI_PASSWORD));

        // Set oc version - this version of the client will be used as the binary client
        System.setProperty("xtf.config.openshift.version", "3.10.70");
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

    /**
     * Used to override syndesis version when the property is defined later than TestConfiguration is initialized.
     * @param version syndesis version
     */
    public void overrideSyndesisVersion(String version) {
        properties.setProperty(SYNDESIS_TEMPLATE_URL, String.format("https://raw.githubusercontent.com/syndesisio/syndesis/%s/install/syndesis.yml", version));
        properties.setProperty(SYNDESIS_TEMPLATE_SA, String.format("https://raw.githubusercontent.com/syndesisio/syndesis/%s/install/support/serviceaccount-as-oauthclient-restricted.yml", version));
        properties.setProperty(SYNDESIS_OPERATOR_CRD_URL, String.format("https://raw.githubusercontent.com/syndesisio/syndesis/%s/install/operator/deploy/syndesis-crd.yml", version));
        properties.setProperty(SYNDESIS_OPERATOR_URL, String.format("https://raw.githubusercontent.com/syndesisio/syndesis/%s/install/operator/deploy/syndesis-operator.yml", version));
        properties.setProperty(SYNDESIS_OPERATOR_TEMPLATE_URL, String.format("https://raw.githubusercontent.com/syndesisio/syndesis/%s/install/operator/deploy/syndesis.yml", version));
    }

}

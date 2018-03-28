package io.syndesis.qe.utils;

import org.apache.http.client.HttpClient;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;

import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.engines.ApacheHttpClient4Engine;
import org.jboss.resteasy.plugins.providers.jackson.ResteasyJackson2Provider;
import org.jboss.resteasy.spi.ResteasyProviderFactory;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;

import javax.ws.rs.client.Client;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.Optional;

import io.fabric8.kubernetes.client.LocalPortForward;
import io.fabric8.openshift.api.model.Route;
import io.syndesis.qe.Component;
import io.syndesis.qe.TestConfiguration;
import io.syndesis.qe.exceptions.RestClientException;
import lombok.extern.slf4j.Slf4j;

/**
 * Utility class for Rest client (RestEasy).
 *
 * @author jknetl
 */
@Slf4j
public final class RestUtils {

    private static LocalPortForward localPortForward = null;
    private static Optional<String> restUrl = Optional.empty();

    private RestUtils() {
    }

    public static Client getClient() throws RestClientException {
        final ResteasyJackson2Provider jackson2Provider = RestUtils.createJacksonProvider(Optional.empty(), Optional.empty());
        return getClient(jackson2Provider);
    }

    public static Client getWrappedClient() throws RestClientException {
        final ResteasyJackson2Provider jackson2Provider = RestUtils.createJacksonProvider(Optional.of(SerializationFeature.WRAP_ROOT_VALUE),
                Optional.of(DeserializationFeature.UNWRAP_ROOT_VALUE));
        return getClient(jackson2Provider);
    }

    public static Client getClient(ResteasyJackson2Provider jackson2Provider) throws RestClientException {
        final ApacheHttpClient4Engine engine = new ApacheHttpClient4Engine(RestUtils.createAllTrustingClient());

        final Client client = new ResteasyClientBuilder()
                .providerFactory(new ResteasyProviderFactory()) // this is needed otherwise default jackson2provider is used, which causes problems with JDK8 Optional
                .register(jackson2Provider)
                .httpEngine(engine)
                .build();

        return client;
    }

    private static ResteasyJackson2Provider createJacksonProvider(Optional<SerializationFeature> serialization, Optional<DeserializationFeature> deserialization) {
        final ResteasyJackson2Provider jackson2Provider = new ResteasyJackson2Provider();
        final ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new Jdk8Module());
        if (serialization.isPresent()) {
            objectMapper.enable(serialization.get());
        }
        if (deserialization.isPresent()) {
            objectMapper.enable(deserialization.get());
        }
        jackson2Provider.setMapper(objectMapper);
        return jackson2Provider;
    }

    //Required in order to skip certificate validation
    private static HttpClient createAllTrustingClient() throws RestClientException {
        HttpClient httpclient = null;
        try {
            final SSLContextBuilder builder = new SSLContextBuilder();
            builder.loadTrustMaterial((TrustStrategy) (X509Certificate[] chain, String authType) -> true);
            final SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
                    builder.build());
            httpclient = HttpClients
                    .custom()
                    .setSSLSocketFactory(sslsf)
                    .setMaxConnTotal(1000)
                    .setMaxConnPerRoute(1000)
                    .build();
        } catch (NoSuchAlgorithmException | KeyStoreException | KeyManagementException e) {
            throw new RestClientException("Cannot create all SSL certificates trusting client", e);
        }
        return httpclient;
    }

    public static String getRestUrl() {

        //TODO(tplevko): before the rest route is generated, check whether it is live. If not, add wait and retry several times
        // and then after some attempts recreate
        if (!restUrl.isPresent()) {
            if (TestConfiguration.useServerRoute()) {
                setupRestPodRoute();
            } else {
                setupLocalPortForward();
            }
        }
        return restUrl.get();
    }

    public static void setupRestPodRoute() {
        Route route = OpenShiftUtils.createRestRoute(TestConfiguration.openShiftNamespace(), TestConfiguration.openShiftRouteSuffix());
        restUrl = Optional.of(String.format("https://%s", route.getSpec().getHost()));
        log.debug("rest endpoint URL: " + restUrl.get());
    }

    public static void setupLocalPortForward() {
        if (localPortForward == null || !localPortForward.isAlive()) {
            log.debug("creating local port forward for pod syndesis-server");
            localPortForward = TestUtils.createLocalPortForward(Component.SERVER.getName(), 8080, 8080);
            try {
                restUrl = Optional.of(String.format("http://%s:%s", localPortForward.getLocalAddress().getLoopbackAddress().getHostName(), localPortForward.getLocalPort()));
            } catch (IllegalStateException ex) {
                restUrl = Optional.of(String.format("http://%s:%s", "127.0.0.1", 8080));
            }
            log.debug("rest endpoint URL: " + restUrl.get());
        }
    }
}

package io.syndesis.qe.utils;

import io.syndesis.qe.Component;
import io.syndesis.qe.exceptions.RestClientException;

import org.apache.commons.io.IOUtils;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.engines.ApacheHttpClient43Engine;
import org.jboss.resteasy.client.jaxrs.internal.LocalResteasyProviderFactory;
import org.jboss.resteasy.plugins.providers.InputStreamProvider;
import org.jboss.resteasy.plugins.providers.StringTextStar;
import org.jboss.resteasy.plugins.providers.jackson.ResteasyJackson2Provider;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataWriter;
import org.jboss.resteasy.spi.ResteasyProviderFactory;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientResponseContext;
import javax.ws.rs.client.ClientResponseFilter;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import io.fabric8.kubernetes.client.LocalPortForward;
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

    private static final String CHECK_URL = "http://localhost:8080/api/v1/version";

    private RestUtils() {
    }

    public static Client getClient() throws RestClientException {
        final ResteasyJackson2Provider jackson2Provider = RestUtils.createJackson2Provider(Optional.empty(), Optional.empty());
        return getClient(jackson2Provider);
    }

    public static Client getInsecureClient() throws RestClientException {
        ClientBuilder clientBuilder = ClientBuilder.newBuilder();
        clientBuilder.connectTimeout(120, TimeUnit.SECONDS);
        clientBuilder.readTimeout(120, TimeUnit.SECONDS);

        final Client client = clientBuilder.build();
        client.register(new ErrorLogger());
        return client;
    }

    public static Client getWrappedClient() throws RestClientException {
        final ResteasyJackson2Provider jackson2Provider = RestUtils.createJackson2Provider(Optional.of(SerializationFeature.WRAP_ROOT_VALUE),
            Optional.of(DeserializationFeature.UNWRAP_ROOT_VALUE));
        return getClient(jackson2Provider);
    }

    public static Client getClient(ResteasyJackson2Provider jackson2Provider) throws RestClientException {
        final ApacheHttpClient43Engine engine = new ApacheHttpClient43Engine(RestUtils.createAllTrustingClient());

        final ResteasyProviderFactory providerFactory = new LocalResteasyProviderFactory();
        providerFactory.register(jackson2Provider)
            .register(new InputStreamProvider()) // needed for GET application/octet-stream in PublicAPI to export zip
            .register(new MultipartFormDataWriter()) // needed to POST mutipart form data (necessary for API provider + PublicAPI)
            .register(new StringTextStar()) // needed to serialize text/plain (again for API provider)
            .register(new ErrorLogger());

        ResteasyClientBuilder clientBuilder = (ResteasyClientBuilder) ResteasyClientBuilder.newBuilder();
        clientBuilder.providerFactory(providerFactory);
        clientBuilder.httpEngine(engine);
        return clientBuilder.build();
    }

    private static ResteasyJackson2Provider createJackson2Provider(Optional<SerializationFeature> serialization,
        Optional<DeserializationFeature> deserialization) {
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
        HttpClient httpclient;
        try {
            final SSLContextBuilder builder = new SSLContextBuilder();
            builder.loadTrustMaterial((TrustStrategy) (X509Certificate[] chain, String authType) -> true);
            final SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
                builder.build(),
                new NoopHostnameVerifier()); // needed to connections to API Provider integrations
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
        if (!restUrl.isPresent()) {
            setupLocalPortForward();
            waitForPortForward();
        } else {
            // Check if the port forward is working
            HTTPResponse httpResponse = null;
            try {
                httpResponse = HttpUtils.doGetRequest(CHECK_URL);
            } catch (Exception ignore) {
                // ignore
            }
            if (httpResponse == null || httpResponse.getCode() != 200) {
                log.error("Port-forward was created, but seems it isn't working, recreating it");
                setupLocalPortForward();
                waitForPortForward();
            }
        }
        return restUrl.get();
    }

    private static void waitForPortForward() {
        TestUtils.waitFor(() -> {
            HTTPResponse response = null;
            try {
                response = HttpUtils.doGetRequest(CHECK_URL);
            } catch (Exception ignore) {
                // ignore
            }
            return response != null && response.getCode() == 200;
        }, 5, 90, "Port-forward not working after 90 seconds");
        TestUtils.sleepIgnoreInterrupt(15000L);
    }

    public static void setupLocalPortForward() {
        if (localPortForward != null) {
            try {
                localPortForward.close();
            } catch (IOException e) {
                log.error("Unable to terminate local port forward: ", e);
            }
        }
        log.debug("creating local port forward for pod syndesis-server");
        localPortForward = TestUtils.createLocalPortForward(Component.SERVER.getName(), 8080, 8080);
        // If there was no pod, do nothing
        if (localPortForward == null) {
            return;
        }
        try {
            restUrl = Optional.of(String
                .format("http://%s:%s", localPortForward.getLocalAddress().getLoopbackAddress().getHostName(), localPortForward.getLocalPort()));
        } catch (IllegalStateException ex) {
            restUrl = Optional.of(String.format("http://%s:%s", "127.0.0.1", 8080));
        }
        log.debug("rest endpoint URL: " + restUrl.get());
    }

    /**
     * Resets the URL and port-forward.
     */
    public static void reset() {
        restUrl = Optional.empty();
        TestUtils.terminateLocalPortForward(localPortForward);
        localPortForward = null;
    }

    /**
     * Logs request and response when response code is bigger than 299.
     */
    private static class ErrorLogger implements ClientResponseFilter {
        @Override
        public void filter(ClientRequestContext requestContext, ClientResponseContext responseContext) throws IOException {
            if (responseContext.getStatus() > 299 && !requestContext.getUri().toString().contains("reset-db")) {
                log.error("Error while invoking " + requestContext.getUri().toString());
                log.error("  Request:");
                log.error("    Headers:");
                requestContext.getStringHeaders().forEach((key, value) -> log.error("      " + key + ":" + value));
                log.error("    Body:");
                if (requestContext.getEntity() != null) {
                    log.error("      " + requestContext.getEntity().toString());
                } else {
                    log.error("      Null");
                }
                log.error("  Response:");
                log.error("    Headers:");
                responseContext.getHeaders().forEach((key, value) -> log.error("      " + key + ":" + value));
                log.error("    Body:");
                if (responseContext.getEntityStream() != null) {
                    log.error("      " + IOUtils.toString(responseContext.getEntityStream(), "UTF-8"));
                } else {
                    log.error("      Null");
                }
            }
        }
    }
}

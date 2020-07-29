package io.syndesis.qe.endpoint.client;

import org.apache.commons.io.IOUtils;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.engines.ApacheHttpClient4Engine;
import org.jboss.resteasy.plugins.providers.InputStreamProvider;
import org.jboss.resteasy.plugins.providers.StringTextStar;
import org.jboss.resteasy.plugins.providers.jackson.ResteasyJackson2Provider;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataWriter;
import org.jboss.resteasy.spi.ResteasyProviderFactory;
import org.springframework.web.client.RestClientException;

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
import java.util.concurrent.TimeUnit;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class EndpointClient {
    //Required in order to skip certificate validation
    private static HttpClient createAllTrustingClient() {
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

    public static Client getClient(ResteasyJackson2Provider jackson2Provider) {
        return new ResteasyClientBuilder()
            .providerFactory(
                new ResteasyProviderFactory()) // this is needed otherwise default jackson2provider is used, which causes problems with JDK8 Optional
            .register(jackson2Provider)
            .register(new InputStreamProvider()) // needed for GET application/octet-stream in PublicAPI to export zip
            .register(new MultipartFormDataWriter()) // needed to POST mutipart form data (necessary for API provider + PublicAPI)
            .register(new StringTextStar()) // needed to serialize text/plain (again for API provider)
            .register(new ErrorLogger())
            .httpEngine(new ApacheHttpClient4Engine(createAllTrustingClient()))
            .build();
    }

    public static Client getClient() {
        return getClient(createJackson2Provider(null, null));
    }

    private static ResteasyJackson2Provider createJackson2Provider(SerializationFeature serialization, DeserializationFeature deserialization) {
        final ResteasyJackson2Provider jackson2Provider = new ResteasyJackson2Provider();
        final ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new Jdk8Module());
        if (serialization != null) {
            objectMapper.enable(serialization);
        }
        if (deserialization != null) {
            objectMapper.enable(deserialization);
        }
        jackson2Provider.setMapper(objectMapper);
        return jackson2Provider;
    }

    public static Client getWrappedClient() {
        return getClient(createJackson2Provider(SerializationFeature.WRAP_ROOT_VALUE, DeserializationFeature.UNWRAP_ROOT_VALUE));
    }

    public static Client getInsecureClient() throws RestClientException {
        ClientBuilder clientBuilder = ClientBuilder.newBuilder();
        clientBuilder.connectTimeout(120, TimeUnit.SECONDS);
        clientBuilder.readTimeout(120, TimeUnit.SECONDS);

        final Client client = clientBuilder.build();
        client.register(new ErrorLogger());
        return client;
    }
}

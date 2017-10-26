package io.syndesis.qe.rest.utils;

import org.apache.http.client.HttpClient;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;

import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.engines.ApacheHttpClient4Engine;
import org.jboss.resteasy.plugins.providers.jackson.ResteasyJackson2Provider;
import org.jboss.resteasy.spi.ResteasyProviderFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;

import javax.ws.rs.client.Client;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;

import io.syndesis.qe.rest.exceptions.RestClientException;

/**
 * Utility class for Rest client (RestEasy).
 *
 * @author jknetl
 */
public final class RestUtils {

	private RestUtils() {
	}

	public static Client getClient() throws RestClientException {
		final ResteasyJackson2Provider jackson2Provider = RestUtils.createJacksonProvider();
		final ApacheHttpClient4Engine engine = new ApacheHttpClient4Engine(RestUtils.createAllTrustingClient());

		final Client client = new ResteasyClientBuilder()
				.providerFactory(new ResteasyProviderFactory()) // this is needed otherwise default jackson2provider is used, which causes problems with JDK8 Optional
				.register(jackson2Provider)
				.httpEngine(engine)
				.build();

		return client;
	}

	private static ResteasyJackson2Provider createJacksonProvider() {
		final ResteasyJackson2Provider jackson2Provider = new ResteasyJackson2Provider();
		final ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.registerModule(new Jdk8Module());
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
}

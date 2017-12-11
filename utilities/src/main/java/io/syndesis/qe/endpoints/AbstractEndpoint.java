package io.syndesis.qe.endpoints;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;

import io.syndesis.model.ListResult;
import io.syndesis.qe.TestConfiguration;
import io.syndesis.qe.utils.RestUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * Implements a client endpoint for syndesis REST.
 *
 * @author jknetl
 */
@Slf4j
public abstract class AbstractEndpoint<T> {

	protected String endpointName;
	protected String syndesisUrl;
	protected String apiPath = TestConfiguration.syndesisRestApiPath();
	private Class<T> type;
	private final Client client;

	public AbstractEndpoint(Class<?> type, String syndesisUrl, String endpointName) throws GeneralSecurityException {
		this.type = (Class<T>) type;
		this.syndesisUrl = syndesisUrl;
		this.endpointName = endpointName;

		client = RestUtils.getClient();
	}

	public T create(T obj) {
		log.debug("POST: {}", getEndpointUrl());
		final Invocation.Builder invocation = client
				.target(getEndpointUrl())
				.request(MediaType.APPLICATION_JSON)
				.header("X-Forwarded-User", "pista")
				.header("X-Forwarded-Access-Token", "kral");

		final Response response = invocation.post(Entity.entity(obj, MediaType.APPLICATION_JSON));

		return response.readEntity(type);
	}

	public void delete(String id) {
		log.debug("DELETE: {}", getEndpointUrl() + "/" + id);
		final Invocation.Builder invocation = client
				.target(getEndpointUrl() + "/" + id)
				.request(MediaType.APPLICATION_JSON)
				.header("X-Forwarded-User", "pista")
				.header("X-Forwarded-Access-Token", "kral");

		final Response response = invocation.delete();
	}

	public T get(String id) {
		log.debug("GET : {}", getEndpointUrl() + "/" + id);
		final Invocation.Builder invocation = client
				.target(getEndpointUrl() + "/" + id)
				.request(MediaType.APPLICATION_JSON)
				.header("X-Forwarded-User", "pista")
				.header("X-Forwarded-Access-Token", "kral");
		final Response response = invocation
				.get();

		final T result = response.readEntity(type);

		return result;
	}

	public T update(String id, T obj) {
		log.debug("PUT : {}", getEndpointUrl() + "/" + id);
		final Invocation.Builder invocation = client
				.target(getEndpointUrl() + "/" + id)
				.request(MediaType.APPLICATION_JSON)
				.header("X-Forwarded-User", "pista")
				.header("X-Forwarded-Access-Token", "kral");
		final Response response = invocation.put(Entity.entity(obj, MediaType.APPLICATION_JSON));

		return response.readEntity(type);
	}

	public List<T> list() {
		final ObjectMapper mapper = new ObjectMapper().registerModules(new Jdk8Module());
		final ObjectWriter ow = mapper.writer();
		final Class<ListResult<T>> listtype = (Class) ListResult.class;

		log.debug("GET : {}", getEndpointUrl());
		final Invocation.Builder invocation = client
				.target(getEndpointUrl())
				.request(MediaType.APPLICATION_JSON)
				.header("X-Forwarded-User", "pista")
				.header("X-Forwarded-Access-Token", "kral");

		final Response response = invocation
				.get();

		//TODO(tplevko): nasty hack... Change this
		final ListResult<T> result = response.readEntity(listtype);
		final List<T> ts = new ArrayList<>();

		for (int i = 0; i < result.getTotalCount(); i++) {
			T con = null;
			try {
				final String json = ow.writeValueAsString(result.getItems().get(i));
				con = (T) mapper.readValue(json, type);
			} catch (IOException ex) {
				log.error(ex.toString());
			}
			ts.add(con);
		}

		return ts;
	}

	public String getEndpointUrl() {
		return String.format("%s%s%s", syndesisUrl, apiPath, endpointName);
	}
}

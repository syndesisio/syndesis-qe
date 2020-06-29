package io.syndesis.qe.endpoint.util;

import io.syndesis.qe.util.Util;

import javax.ws.rs.client.AsyncInvoker;
import javax.ws.rs.client.CompletionStageRxInvoker;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.RxInvoker;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import java.util.Locale;

import lombok.extern.slf4j.Slf4j;

/**
 * Retries the invocation for http methods get, post, put, delete given amount of times in case of any exception.
 */
@Slf4j
public class RetryingInvocationBuilder implements Invocation.Builder {
    private static final int RETRIES_COUNT = 1;

    private Invocation.Builder invocationBuilder;

    public RetryingInvocationBuilder(Invocation.Builder invocationBuilder) {
        this.invocationBuilder = invocationBuilder;
    }

    @Override
    public Invocation build(String method) {
        return invocationBuilder.build(method);
    }

    @Override
    public Invocation build(String method, Entity<?> entity) {
        return invocationBuilder.build(method, entity);
    }

    @Override
    public Invocation buildGet() {
        return invocationBuilder.buildGet();
    }

    @Override
    public Invocation buildDelete() {
        return invocationBuilder.buildDelete();
    }

    @Override
    public Invocation buildPost(Entity<?> entity) {
        return invocationBuilder.buildPost(entity);
    }

    @Override
    public Invocation buildPut(Entity<?> entity) {
        return invocationBuilder.buildPut(entity);
    }

    @Override
    public AsyncInvoker async() {
        return invocationBuilder.async();
    }

    @Override
    public Invocation.Builder accept(String... mediaTypes) {
        return invocationBuilder.accept(mediaTypes);
    }

    @Override
    public Invocation.Builder accept(MediaType... mediaTypes) {
        return invocationBuilder.accept(mediaTypes);
    }

    @Override
    public Invocation.Builder acceptLanguage(Locale... locales) {
        return invocationBuilder.acceptLanguage(locales);
    }

    @Override
    public Invocation.Builder acceptLanguage(String... locales) {
        return invocationBuilder.acceptLanguage(locales);
    }

    @Override
    public Invocation.Builder acceptEncoding(String... encodings) {
        return invocationBuilder.acceptEncoding(encodings);
    }

    @Override
    public Invocation.Builder cookie(Cookie cookie) {
        return invocationBuilder.cookie(cookie);
    }

    @Override
    public Invocation.Builder cookie(String name, String value) {
        return invocationBuilder.cookie(name, value);
    }

    @Override
    public Invocation.Builder cacheControl(CacheControl cacheControl) {
        return invocationBuilder.cacheControl(cacheControl);
    }

    @Override
    public Invocation.Builder header(String name, Object value) {
        return invocationBuilder.header(name, value);
    }

    @Override
    public Invocation.Builder headers(MultivaluedMap<String, Object> headers) {
        return invocationBuilder.headers(headers);
    }

    @Override
    public Invocation.Builder property(String name, Object value) {
        return invocationBuilder.property(name, value);
    }

    @Override
    public CompletionStageRxInvoker rx() {
        return invocationBuilder.rx();
    }

    @Override
    public <T extends RxInvoker> T rx(Class<T> clazz) {
        return invocationBuilder.rx(clazz);
    }

    @Override
    public Response get() {
        return (Response) retryingInvoke(buildGet(), null);
    }

    @Override
    public <T> T get(Class<T> responseType) {
        return (T) retryingInvoke(buildGet(), responseType);
    }

    @Override
    public <T> T get(GenericType<T> responseType) {
        return (T) retryingInvoke(buildGet(), responseType);
    }

    @Override
    public Response put(Entity<?> entity) {
        return (Response) retryingInvoke(buildPut(entity), null);
    }

    @Override
    public <T> T put(Entity<?> entity, Class<T> responseType) {
        return (T) retryingInvoke(buildPut(entity), responseType);
    }

    @Override
    public <T> T put(Entity<?> entity, GenericType<T> responseType) {
        return (T) retryingInvoke(buildPut(entity), responseType);
    }

    @Override
    public Response post(Entity<?> entity) {
        return (Response) retryingInvoke(buildPost(entity), null);
    }

    @Override
    public <T> T post(Entity<?> entity, Class<T> responseType) {
        return (T) retryingInvoke(buildPost(entity), responseType);
    }

    @Override
    public <T> T post(Entity<?> entity, GenericType<T> responseType) {
        return (T) retryingInvoke(buildPost(entity), responseType);
    }

    @Override
    public Response delete() {
        return (Response) retryingInvoke(buildDelete(), null);
    }

    @Override
    public <T> T delete(Class<T> responseType) {
        return (T) retryingInvoke(buildDelete(), responseType);
    }

    @Override
    public <T> T delete(GenericType<T> responseType) {
        return (T) retryingInvoke(buildDelete(), responseType);
    }

    @Override
    public Response head() {
        return invocationBuilder.head();
    }

    @Override
    public Response options() {
        return invocationBuilder.options();
    }

    @Override
    public <T> T options(Class<T> responseType) {
        return invocationBuilder.options(responseType);
    }

    @Override
    public <T> T options(GenericType<T> responseType) {
        return invocationBuilder.options(responseType);
    }

    @Override
    public Response trace() {
        return invocationBuilder.trace();
    }

    @Override
    public <T> T trace(Class<T> responseType) {
        return invocationBuilder.trace(responseType);
    }

    @Override
    public <T> T trace(GenericType<T> responseType) {
        return invocationBuilder.trace(responseType);
    }

    @Override
    public Response method(String name) {
        return invocationBuilder.method(name);
    }

    @Override
    public <T> T method(String name, Class<T> responseType) {
        return invocationBuilder.method(name, responseType);
    }

    @Override
    public <T> T method(String name, GenericType<T> responseType) {
        return invocationBuilder.method(name, responseType);
    }

    @Override
    public Response method(String name, Entity<?> entity) {
        return invocationBuilder.method(name, entity);
    }

    @Override
    public <T> T method(String name, Entity<?> entity, Class<T> responseType) {
        return invocationBuilder.method(name, entity, responseType);
    }

    @Override
    public <T> T method(String name, Entity<?> entity, GenericType<T> responseType) {
        return invocationBuilder.method(name, entity, responseType);
    }

    /**
     * Invokes the invocation. If the invocation throws an exception, it will be retried.
     *
     * @param i invocation
     * @param responseType response type object
     * @return object (Response or responseType class)
     */
    private Object retryingInvoke(Invocation i, Object responseType) {
        int retries = 0;
        do {
            try {
                if (responseType == null) {
                    return i.invoke();
                } else if (responseType instanceof GenericType) {
                    return i.invoke((GenericType) responseType);
                } else {
                    return i.invoke((Class) responseType);
                }
            } catch (Exception e) {
                log.error("Exception raised during method invocation, will retry " + (RETRIES_COUNT - retries) + " more times", e);
                retries++;
                Util.sleep(10000L);
            }
        } while (retries <= RETRIES_COUNT);
        throw new RuntimeException("Unable to invoke endpoint, see logs");
    }
}

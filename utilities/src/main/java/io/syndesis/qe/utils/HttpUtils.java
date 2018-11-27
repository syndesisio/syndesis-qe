package io.syndesis.qe.utils;

import static org.assertj.core.api.Assertions.fail;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.concurrent.TimeUnit;

import lombok.extern.slf4j.Slf4j;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

@Slf4j
public final class HttpUtils {
    public enum Method {
        GET, POST, PUT, DELETE
    }

    private HttpUtils() {
    }

    public static Response doPostRequest(String url, String content) {
        return doPostRequest(url, content, "application/json", null);
    }

    public static Response doPostRequest(String url, String content, Headers headers) {
        return doPostRequest(url, content, "application/json", headers);
    }

    public static Response doPostRequest(String url, String content, String contentType, Headers headers) {
        RequestBody body = RequestBody.create(MediaType.parse(contentType), content);
        return doPostRequest(url, body, headers);
    }

    public static Response doPostRequest(String url, RequestBody body, Headers headers) {
        Request.Builder requestBuilder = new Request.Builder()
                .url(url)
                .post(body);
        if (headers != null) {
            requestBuilder.headers(headers);
        }

        try {
            return getClient().newCall(requestBuilder.build()).execute();
        } catch (IOException e) {
            log.error("Post invocation failed!", e);
            //print whole stacktrace
            e.printStackTrace();
        }
        return null;
    }

    public static Response doGetRequest(String url) {
        return doGetRequest(url, null);
    }

    public static Response doGetRequest(String url, Headers headers) {
        Request.Builder requestBuilder = new Request.Builder()
                .url(url)
                .get();
        if (headers != null) {
            requestBuilder.headers(headers);
        }

        try {
            return getClient().newCall(requestBuilder.build()).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Response doDeleteRequest(String url) {
        return doDeleteRequest(url, null);
    }

    public static Response doDeleteRequest(String url, Headers headers) {
        Request.Builder requestBuilder = new Request.Builder()
                .url(url)
                .delete();
        if (headers != null) {
            requestBuilder.headers(headers);
        }

        try {
            return getClient().newCall(requestBuilder.build()).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Response doPutRequest(String url, String content, Headers headers) {
        return doPutRequest(url, content, "application/json", headers);
    }

    public static Response doPutRequest(String url, String content, String contentType) {
        return doPutRequest(url, content, contentType, null);
    }

    public static Response doPutRequest(String url, String content, String contentType, Headers headers) {
        RequestBody body = RequestBody.create(MediaType.parse(contentType), content);
        Request.Builder requestBuilder = new Request.Builder()
                .url(url)
                .put(body);
        if (headers != null) {
            requestBuilder.headers(headers);
        }

        try {
            return getClient().newCall(requestBuilder.build()).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static OkHttpClient getClient() {
        // Create a trust manager that does not validate certificate chains
        final TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    @Override
                    public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                    }

                    @Override
                    public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                    }

                    @Override
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                        return new java.security.cert.X509Certificate[]{};
                    }
                }
        };

        // Install the all-trusting trust manager
        final SSLContext sslContext;
        try {
            sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
            final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
            OkHttpClient.Builder builder = new OkHttpClient.Builder();
            builder.sslSocketFactory(sslSocketFactory, (X509TrustManager) trustAllCerts[0]);
            builder.hostnameVerifier((hostname, session) -> true);
            return builder
                    .connectTimeout(15, TimeUnit.SECONDS)
                    .writeTimeout(15, TimeUnit.SECONDS)
                    .readTimeout(15, TimeUnit.SECONDS)
                    .build();
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            fail("Error while creating Http client", e);
        }
        return null;
    }
}

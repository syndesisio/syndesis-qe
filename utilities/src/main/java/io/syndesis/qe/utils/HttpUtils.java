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

    public static HTTPResponse doPostRequest(String url, String content) {
        return doPostRequest(url, content, "application/json", null);
    }

    public static HTTPResponse doPostRequest(String url, String content, Headers headers) {
        return doPostRequest(url, content, "application/json", headers);
    }

    public static HTTPResponse doPostRequest(String url, String content, String contentType, Headers headers) {
        RequestBody body = RequestBody.create(MediaType.parse(contentType), content);
        return doPostRequest(url, body, headers);
    }

    private static HTTPResponse doRequest(Request request) {
        try {
            Response r = getClient().newCall(request).execute();
            return new HTTPResponse(r.body().string(), r.code());
        } catch (IOException e) {
            log.error("Request invocation failed!", e);
            //print whole stacktrace
            e.printStackTrace();
        }
        return null;
    }

    public static HTTPResponse doPostRequest(String url, RequestBody body, Headers headers) {
        Request.Builder requestBuilder = new Request.Builder()
            .url(url)
            .post(body);
        if (headers != null) {
            requestBuilder.headers(headers);
        }

        return doRequest(requestBuilder.build());
    }

    public static HTTPResponse doGetRequest(String url) {
        return doGetRequest(url, null);
    }

    public static HTTPResponse doGetRequest(String url, Headers headers) {
        Request.Builder requestBuilder = new Request.Builder()
            .url(url)
            .get();
        if (headers != null) {
            requestBuilder.headers(headers);
        }

        return doRequest(requestBuilder.build());
    }

    public static HTTPResponse doDeleteRequest(String url) {
        return doDeleteRequest(url, null);
    }

    public static HTTPResponse doDeleteRequest(String url, Headers headers) {
        Request.Builder requestBuilder = new Request.Builder()
            .url(url)
            .delete();
        if (headers != null) {
            requestBuilder.headers(headers);
        }

        return doRequest(requestBuilder.build());
    }

    public static HTTPResponse doPutRequest(String url, String content, Headers headers) {
        return doPutRequest(url, content, "application/json", headers);
    }

    public static HTTPResponse doPutRequest(String url, String content, String contentType) {
        return doPutRequest(url, content, contentType, null);
    }

    public static HTTPResponse doPutRequest(String url, String content, String contentType, Headers headers) {
        RequestBody body = RequestBody.create(MediaType.parse(contentType), content);
        Request.Builder requestBuilder = new Request.Builder()
            .url(url)
            .put(body);
        if (headers != null) {
            requestBuilder.headers(headers);
        }

        return doRequest(requestBuilder.build());
    }

    /**
     * Check if the url is reachable.
     * @param url url to check
     * @return true if there is any response, false if there is an exception raised
     */
    public static boolean isReachable(String url) {
        Request.Builder requestBuilder = new Request.Builder()
            .url(url)
            .get();
        try {
            Response r = getClient().newCall(requestBuilder.build()).execute();
            // Close the body to prevent leaked connections
            r.body().close();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private static OkHttpClient getClient() {
        // Create a trust manager that does not validate certificate chains
        final TrustManager[] trustAllCerts = new TrustManager[] {
            new X509TrustManager() {
                @Override
                public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                }

                @Override
                public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                }

                @Override
                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                    return new java.security.cert.X509Certificate[] {};
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
                .connectTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            fail("Error while creating Http client", e);
        }
        return null;
    }
}

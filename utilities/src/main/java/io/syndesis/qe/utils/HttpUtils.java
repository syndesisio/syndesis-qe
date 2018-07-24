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

import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public final class HttpUtils {
    private HttpUtils() {
    }

    public static Response doPostRequest(String url, String content) {
        return doPostRequest(url, content, "application/json", null);
    }

    public static Response doPostRequest(String url, String content, String contentType, Headers headers) {
        RequestBody body = RequestBody.create(MediaType.parse(contentType), content);
        Request.Builder requestBuilder = new Request.Builder()
                .url(url)
                .post(body);
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

    public static Response doGetRequest(String url) {
        Request.Builder requestBuilder = new Request.Builder()
                .url(url)
                .get();
        try {
            return getClient().newCall(requestBuilder.build()).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Response doDeleteRequest(String url) {
        Request.Builder requestBuilder = new Request.Builder()
                .url(url)
                .delete();
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

package io.syndesis.qe.steps.other;

import cucumber.api.java.en.When;
import io.syndesis.qe.TestConfiguration;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.concurrent.TimeUnit;

import static io.fabric8.kubernetes.client.dsl.base.OperationSupport.JSON;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

@Slf4j
public class InvokeHtmlRequest {

    /**
     * @param webhookToken token set when creating the integration
     * @param body
     * @throws IOException
     */
    @When("^.*invoke post request to integration \"([^\"]*)\" with webhook \"([^\"]*)\" and body (.*)$")
    public void invokeRequest(String integrationName, String webhookToken, String body) throws IOException {
        log.info("Body to set: " + body);
        //example of webhook url: "https://i-webhook-test-syndesis.192.168.42.2.nip.io/webhook/test-webhook"
        String combinedUrl = TestConfiguration.syndesisUrl()
                .replace("syndesis", "i-" + integrationName + "-syndesis") + "/webhook/" + webhookToken;
        log.info("Combined URL: " + combinedUrl);

        assertThat(doPostRequest(combinedUrl, body))
                .isEqualTo(204);
    }

    private int doPostRequest(String url, String json) throws IOException {
        RequestBody body = RequestBody.create(JSON, json);
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        Response response = getClient().newCall(request).execute();
        return response.code();
    }

    private OkHttpClient getClient() {
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

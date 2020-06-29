package io.syndesis.qe.utils.jira;

import io.syndesis.qe.account.Account;
import io.syndesis.qe.utils.AccountUtils;

import org.assertj.core.api.Assertions;

import com.atlassian.httpclient.api.Request;
import com.atlassian.jira.rest.client.api.AuthenticationHandler;
import com.google.api.client.auth.oauth.AbstractOAuthGetToken;
import com.google.api.client.auth.oauth.OAuthParameters;
import com.google.api.client.auth.oauth.OAuthRsaSigner;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.apache.ApacheHttpTransport;
import com.google.api.client.util.Base64;

import java.lang.reflect.Field;
import java.net.URI;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class OAuthJiraAuthenticationHandler implements AuthenticationHandler {
    @Override
    public void configure(Request.Builder builder) {
        OAuthParameters parameters;
        try {
            parameters = createAccessToken().createParameters();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            Assertions.fail("Could not create OAuthParameters for use by jira client");
            return;
        }

        URI uri;
        String method;
        try {
            Field i = builder.getClass().getSuperclass().getDeclaredField("uri");
            i.setAccessible(true);
            uri = (URI) i.get(builder);
            i = builder.getClass().getSuperclass().getDeclaredField("method");
            i.setAccessible(true);
            method = ((Request.Method) i.get(builder)).name();
        } catch (NoSuchFieldException | IllegalAccessException e) {
            Assertions.fail("Could not get URI and http method from request by jira client");
            return;
        }

        parameters.computeNonce();
        parameters.computeTimestamp();
        try {
            parameters.computeSignature(method, new GenericUrl(uri));
        } catch (GeneralSecurityException e) {
            Assertions.fail("Could not compute OAuth signature for jira client");
            return;
        }

        builder.setHeader("Authorization", parameters.getAuthorizationHeader());
    }

    private AbstractOAuthGetToken createAccessToken() throws NoSuchAlgorithmException, InvalidKeySpecException {
        Account jiraAccount = AccountUtils.get("Jira");
        String privateKey = jiraAccount.getProperty("privatekey");
        String tempToken = jiraAccount.getProperty("accesstoken");
        String verifier = jiraAccount.getProperty("verificationcode");
        String consumerKey = jiraAccount.getProperty("consumerkey");
        String jiraUrl = jiraAccount.getProperty("jiraurl");

        byte[] privateBytes = Base64.decodeBase64(privateKey);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(privateBytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");

        OAuthRsaSigner oAuthRsaSigner = new OAuthRsaSigner();
        oAuthRsaSigner.privateKey = kf.generatePrivate(keySpec);

        JiraOAuthGetAccessToken accessToken = new JiraOAuthGetAccessToken(jiraUrl + "/plugins/servlet/oauth/access-token");
        accessToken.temporaryToken = tempToken;
        accessToken.signer = oAuthRsaSigner;
        accessToken.transport = new ApacheHttpTransport();
        accessToken.verifier = verifier;
        accessToken.consumerKey = consumerKey;
        return accessToken;

    }
}

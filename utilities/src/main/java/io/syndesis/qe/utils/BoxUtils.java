package io.syndesis.qe.utils;

import static org.assertj.core.api.Fail.fail;

import io.syndesis.qe.accounts.Account;
import io.syndesis.qe.accounts.AccountsDirectory;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.FormElement;
import org.jsoup.select.Elements;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import com.box.sdk.BoxAPIConnection;
import com.box.sdk.BoxFile;
import com.box.sdk.BoxFolder;
import com.box.sdk.BoxItem;

import javax.annotation.PostConstruct;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * Class with various box helper methods.
 */
@Slf4j
@Component
@Lazy
public class BoxUtils {
    private static final String OAUTH_URL = "https://account.box.com/api/oauth2/authorize?response_type=code&client_id=";

    private Account boxAccount;
    @Getter
    private BoxAPIConnection boxConnection;
    @Getter
    private static List<String> fileIds = new ArrayList<>();
    private String folderId;

    @PostConstruct
    public void initClient() {
        log.info("Getting box access token");
        boxAccount = AccountsDirectory.getInstance().getAccount(Account.Name.BOX)
                .orElseThrow(() -> new IllegalArgumentException("Unable to find box account"));
        String accessToken;
        try {
            accessToken = getAccessToken();
        } catch (Exception e) {
            throw new RuntimeException("Unable to obtain box access token!", e);
        }
        boxConnection = new BoxAPIConnection(boxAccount.getProperty("clientId"), boxAccount.getProperty("clientSecret"), accessToken);
        folderId = boxAccount.getProperty("folderId");
    }

    public void clearBox() {
        log.info("Clearing Box folder");
        for (BoxItem.Info itemInfo : new BoxFolder(this.boxConnection, folderId)) {
            log.debug("Deleting file {} (id {})", itemInfo.getName(), itemInfo.getID());
            new BoxFile(this.boxConnection, itemInfo.getID()).delete();
        }
        fileIds.clear();
    }

    public void uploadFile(String name, String content) {
        try {
            BoxFile.Info info = new BoxFolder(this.boxConnection, folderId).uploadFile(
                    IOUtils.toInputStream(content, "UTF-8"), name);
            log.debug("Uploaded file id: " + info.getID());
            fileIds.add(info.getID());
        } catch (Exception ex) {
            fail("Unable to upload file to Box: ", ex);
        }
    }

    public int getFileCount() {
        return ((Collection<?>) new BoxFolder(this.boxConnection, folderId)).size();
    }

    public BoxFile getFile(String name) {
        for (BoxItem.Info info : new BoxFolder(this.getBoxConnection(), folderId)) {
            if (name.equals(info.getName())) {
                return new BoxFile(this.getBoxConnection(), info.getID());
            }
        }
        return null;
    }

    /**
     * From org.apache.camel.component.box.internal.BoxConnectionHelper
     *
     * @return box access token
     */
    private String getAccessToken() throws Exception {
        final String csrfToken = String.valueOf(new SecureRandom().nextLong());
        final String url = OAUTH_URL + boxAccount.getProperty("clientId") + "&state=" + csrfToken;
        final Connection.Response loginPageResponse = Jsoup.connect(url).method(Connection.Method.GET).execute();
        Document loginPage = loginPageResponse.parse();

        validatePage(loginPage);

        final FormElement loginForm = (FormElement) loginPage.select("form[name=login_form]").first();

        final Element loginField = loginForm.select("input[name=login]").first();
        loginField.val(boxAccount.getProperty("userName"));

        final Element passwordField = loginForm.select("input[name=password]").first();
        passwordField.val(boxAccount.getProperty("userPassword"));

        final Map<String, String> cookies = new HashMap<>(loginPageResponse.cookies());
        Connection.Response response = loginForm.submit()
                .cookies(cookies)
                .execute();
        cookies.putAll(response.cookies());
        final Document consentPage = response.parse();

        validatePage(consentPage);

        final FormElement consentForm = (FormElement) consentPage.select("form[name=consent_form]").first();

        consentForm.elements().removeIf(e -> e.attr("name").equals("consent_reject"));
        //parse request_token from javascript from head, it is the first script in the header
        final String requestTokenScript = consentPage.select("script").first().html();
        final String requestToken = StringUtils.substringBetween(requestTokenScript, "'", "'");
        if (requestToken == null) {
            fail("Coudln't parse request token from " + requestTokenScript);
        }
        response = consentForm.submit()
                    .data("request_token", requestToken)
                    .followRedirects(false)
                    .cookies(cookies)
                    .execute();
        final String location = response.header("Location");

        final String code = StringUtils.substringAfterLast(location, "code=");

        if (code == null) {
            fail("Unable to get code from " + location);
        }

        return code;
    }

    /**
     * From org.apache.camel.component.box.internal.BoxConnectionHelper
     *
     * @param page html page
     */
    private static void validatePage(Document page) {
        // CAPTCHA
        Elements captchaDivs = page.select("div[class*=g-recaptcha]");
        if (!captchaDivs.isEmpty()) {
            throw new IllegalArgumentException(
                    "Authentication requires CAPTCHA test. First you need to authenticate the account manually via web to unlock CAPTCHA.");
        }

        // 2-step verification
        Elements twoStepDivs = page.select("div[data-module=two-factor-enroll-form]");
        if (!twoStepDivs.isEmpty()) {
            throw new IllegalArgumentException(
                    "2-step verification is enabled on the Box account. Turn it off for camel-box to proceed the standard authentication.");
        }

        // login failures
        Elements errorDivs = page.select("div[class*=error_message]");
        String errorMessage = null;
        if (!errorDivs.isEmpty()) {
            errorMessage = errorDivs.first().text().replaceAll("\\s+", " ")
                    .replaceAll(" Show Error Details", ":").trim();
        } else {
            errorDivs = page.select("div[class*=message]");
            if (!errorDivs.isEmpty()) {
                errorMessage = errorDivs.first().text();
            }
        }

        if (!errorDivs.isEmpty()) {
            throw new IllegalArgumentException("Error authorizing application: " + errorMessage);
        }
    }
}

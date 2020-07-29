package io.syndesis.qe.utils;

import static com.codeborne.selenide.Selenide.$;

import io.syndesis.qe.TestConfiguration;

import org.openqa.selenium.By;

import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;

import java.io.File;

public class UploadFile {

    /**
     * Uploads a file while possibly working around a limitation in Firefox 60 ESR
     *
     * Firefox 60 ESR does not allow sending keys to hidden file input element
     * (see https://bugzilla.mozilla.org/show_bug.cgi?id=1502864 )
     * the easiest way to workaround is to remove display: none style from the input
     *
     * @param selector the input element selector to upload the file to. Will be made visible on firefox
     * @param file file to upload
     */
    public static void uploadFile(By selector, File file) {
        uploadFile($(selector), file);
    }

    /**
     * Uploads a file while possibly working around a limitation in Firefox 60 ESR
     *
     * @param element the input element to upload the file to. Will be made visible on firefox
     * @param file file to upload
     */
    public static void uploadFile(SelenideElement element, File file) {
        if ("firefox".equals(TestConfiguration.syndesisBrowser())) {
            Selenide.executeJavaScript("arguments[0].style.display='inline'", element);
        }

        element.uploadFile(file);
    }
}

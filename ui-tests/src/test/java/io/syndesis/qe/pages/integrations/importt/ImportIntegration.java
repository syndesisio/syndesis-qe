package io.syndesis.qe.pages.integrations.importt;

import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;

import org.openqa.selenium.By;

import com.codeborne.selenide.SelenideElement;

import java.io.File;

import io.syndesis.qe.CustomWebDriverProvider;
import io.syndesis.qe.pages.SyndesisPageObject;
import io.syndesis.qe.utils.DragAndDropFile;

public class ImportIntegration extends SyndesisPageObject {

    private static final class Element {
        public static final By ROOT = By.cssSelector("syndesis-integration-import-export");
        public static final By FILE_INPUT = By.cssSelector("input[type='file']");
        public static final By FINISHED_PROGRESS_BAR = By.className("pficon-ok");
        public static final By DRAG_AND_DROP_PLACE = By.className("syn-drop-zone");
    }

    @Override
    public SelenideElement getRootElement() {
        return $(Element.ROOT);
    }

    public boolean validate() {
        return getRootElement().is(visible);
    }


    public void importIntegration(String integrationName) throws InterruptedException {

        String filePath = CustomWebDriverProvider.DOWNLOAD_DIR + File.separator + integrationName + "-export.zip";
        File exportedIntegrationFile = new File(filePath);

        getRootElement().find(Element.FILE_INPUT).shouldBe(visible).uploadFile(exportedIntegrationFile);
        $(Element.FINISHED_PROGRESS_BAR).shouldBe(visible);
        getButton("Done").shouldBe(visible).click();
    }

    public void importIntegrationViaDragAndDrop(String integrationName) {

        String filePath = CustomWebDriverProvider.DOWNLOAD_DIR + File.separator + integrationName + "-export.zip";

        DragAndDropFile.dragAndDropFile(new File(filePath),
                $(Element.DRAG_AND_DROP_PLACE).shouldBe(visible),
                Element.FINISHED_PROGRESS_BAR);

        getButton("Done").shouldBe(visible).click();
    }
}

package io.syndesis.qe.pages.integrations.importt;

import static com.codeborne.selenide.Condition.exist;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;

import io.syndesis.qe.CustomWebDriverProvider;
import io.syndesis.qe.pages.SyndesisPageObject;
import io.syndesis.qe.utils.ByUtils;
import io.syndesis.qe.utils.DragAndDropFile;
import io.syndesis.qe.utils.TestUtils;
import io.syndesis.qe.utils.UploadFile;

import org.openqa.selenium.By;

import com.codeborne.selenide.SelenideElement;

import java.io.File;

public class ImportIntegration extends SyndesisPageObject {

    private static final class Element {
        public static final By ROOT = By.className("pf-c-page__main");
        public static final By FILE_INPUT = By.cssSelector("input[type='file']");
        public static final By FINISHED_PROGRESS_BAR =
            ByUtils.containsDataTestId("dnd-file-chooser-successfully-imported-");
        public static final By DRAG_AND_DROP_PLACE = By.className("dnd-file-chooser__helpText");
    }

    @Override
    public SelenideElement getRootElement() {
        return $(Element.ROOT);
    }

    public boolean validate() {
        return getRootElement().is(visible);
    }

    /**
     * Import integration from zip file
     *
     * @param file
     */
    public void importIntegration(File file) {
        UploadFile.uploadFile(getRootElement().find(Element.FILE_INPUT).should(exist), file);
        TestUtils.waitFor(() -> $(Element.FINISHED_PROGRESS_BAR).is(visible), 5, 30, "Import success message is not visible");
    }

    /**
     * Import integration from browsers default download dir
     *
     * @param integrationName name of integration which was exported to browsers default download dir
     */
    public void importIntegration(String integrationName) {

        String filePath = CustomWebDriverProvider.DOWNLOAD_DIR + File.separator + integrationName + "-export.zip";
        File exportedIntegrationFile = new File(filePath);
        importIntegration(exportedIntegrationFile);
    }

    /**
     * Import integration from browsers default download dir via drag'n'drop feature
     *
     * @param integrationName
     */
    public void importIntegrationViaDragAndDrop(String integrationName) {

        String filePath = CustomWebDriverProvider.DOWNLOAD_DIR + File.separator + integrationName + "-export.zip";

        DragAndDropFile.dragAndDropFile(new File(filePath),
            $(Element.DRAG_AND_DROP_PLACE).shouldBe(visible),
            Element.FINISHED_PROGRESS_BAR);

        TestUtils.sleepForJenkinsDelayIfHigher(2);
    }
}

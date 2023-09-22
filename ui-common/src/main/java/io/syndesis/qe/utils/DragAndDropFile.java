package io.syndesis.qe.utils;

import static com.codeborne.selenide.Condition.appears;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.executeJavaScript;

import io.syndesis.qe.pages.ModalDialogPage;

import org.openqa.selenium.By;

import com.codeborne.selenide.SelenideElement;

import java.io.File;
import java.time.Duration;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DragAndDropFile {

    /**
     * Parameter file will be dragged to dragTarget, then method will wait for waitForElement to be visible
     * For now it only works if your drag and drop target is inside ModalDialogPage.
     *
     * @param file
     * @param dragTarget
     * @param waitForElement
     */
    public static void dragAndDropFile(File file, SelenideElement dragTarget, By waitForElement) {

        createInputFile();
        //find newly created file input element so we can put our file there
        SelenideElement fileInput = $("#selenideUpload");
        fileInput.uploadFile(file);

        //selenium dragAndDrop method does not work due to bugs in webdriver - dnd tests are currently disabled TODO
        $("#selenideUpload").dragAndDropTo($(By.className("dnd-file-chooser__selectedFileList")));

        //selenide action does not work
        //        Actions builder = new Actions(getWebDriver());
        //        Action dragAnddrop = builder.clickAndHold(fileInput)
        //            .moveToElement(dragTarget)
        //            .release(dragTarget)
        //            .build();
        //        dragAnddrop.perform();

        //following JS methods do not work anymore - uploaded file has hidden path in javascript due to security reasons
        // and we are unable to add the file to drag event
        //        dispatchFileDragAndDropEvent("dragenter", "document", fileInput, file);
        //        dispatchFileDragAndDropEvent("dragover", "document", fileInput, file);
        //        dispatchFileDragAndDropEvent("drop", dragTarget, fileInput, file);

        // wait until file appears on the page
        $(waitForElement).shouldBe(appears, Duration.ofSeconds(5));

        // remove fake input file element
        executeJavaScript("arguments[0].parentNode.removeChild(arguments[0]);", fileInput);
    }

    /**
     * Generate a fake input file selector which will be later dragged to dragTarget
     */
    public static void createInputFile() {
        executeJavaScript("var input = document.createElement('input');" +
            "input.id = 'selenideUpload';" +
            "input.type = 'file';" +
            "input.style.display = 'block';" +
            "input.style.opacity = '1';" +
            "input.style['transform']='translate(0px, 0px) scale(1)';" +
            "input.style['MozTransform']='translate(0px, 0px) scale(1)';" +
            "input.style['WebkitTransform']='translate(0px, 0px) scale(1)';" +
            "input.style['msTransform']='translate(0px, 0px) scale(1)';" +
            "input.style['OTransform']='translate(0px, 0px) scale(1)';" +
            "input.style.visibility = 'visible';" +
            "input.style.height = '100px';" +
            "input.style.width = '100px';" +
            "input.name = 'uploadfile';" +
            "input.draggable = 'true';" +
            "input.droppable = 'true';" +
            "document.querySelector(\"#root\").insertBefore(input, document.querySelector(\"#root\").children[0]);");
    }

    public static void dispatchFileDragAndDropEvent(String eventName, Object to, SelenideElement fileInputId, File file) {
        String script = "var files = '" + file + "';" +
            "var items = [];" +
            "var types = [];" +
            "for (var i = 0; i < files.length; i++) {" +
            " items[i] = {kind: 'file', type: files[i].type};" +
            " types[i] = 'Files';" +
            "}" +
            "var event = document.createEvent('CustomEvent');" +
            "event.initCustomEvent(arguments[1], true, true, 0);" +
            "event.dataTransfer = {" +
            " files: files," +
            " items: items," +
            " types: types" +
            "};" +
            "arguments[2].dispatchEvent(event);";

        if (to instanceof String) {
            // for "document" in dispatchFileDragAndDropEvent("dragenter", "document", fileInput)
            script = script.replace("arguments[2]", to.toString());
        }

        log.info("Invoking javascript:");
        log.info("Argument0: " + fileInputId.toString());
        log.info("Argument1: " + eventName);
        log.info("Argument2: " + to.toString());
        log.info("Script: *********");
        log.info(script);
        log.info("End of script *********");
        executeJavaScript(script, fileInputId, eventName, to);
    }
}

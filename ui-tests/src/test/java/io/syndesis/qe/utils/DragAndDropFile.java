package io.syndesis.qe.utils;

import com.codeborne.selenide.SelenideElement;
import org.openqa.selenium.By;

import java.io.File;

import static com.codeborne.selenide.Condition.appears;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.executeJavaScript;

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

        dispatchFileDragAndDropEvent("dragenter", "document", fileInput);
        dispatchFileDragAndDropEvent("dragover", "document", fileInput);
        dispatchFileDragAndDropEvent("drop", dragTarget, fileInput);
        // wait until file appears on the page
        $(waitForElement).waitUntil(appears, 5000);
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
                "document.querySelector(\"syndesis-integration-import-page-component\").insertBefore(input, document.querySelector(\"syndesis-integration-import-page-component\").children[0]);");
    }

    public static void dispatchFileDragAndDropEvent(String eventName, Object to, SelenideElement fileInputId) {
        String script = "var files = arguments[0].files;" +
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
        } else {
            executeJavaScript(script, fileInputId, eventName, to);
        }

    }
}

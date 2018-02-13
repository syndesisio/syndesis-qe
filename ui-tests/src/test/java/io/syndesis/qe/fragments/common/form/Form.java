package io.syndesis.qe.fragments.common.form;

import static com.codeborne.selenide.Condition.visible;

import org.openqa.selenium.By;

import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Getter
@Slf4j
public class Form {
    private SelenideElement rootElement;

    public Form(SelenideElement rootElement) {
        this.rootElement = rootElement;
    }

    public void fill(Map<String, String> data) {
        if (data.isEmpty()) {
            throw new IllegalArgumentException("can't find any connection details in connection");
        }

        ElementsCollection inputs = getRootElement().findAll(By.cssSelector("input"));
        List<String> keys = new ArrayList<String>();

        for (SelenideElement input : inputs) {
            String name = input.getAttribute("name");
            keys.add(name);
        }

        for (String key : data.keySet()) {
            if (keys.contains(key)) {
                log.info("fill connection Details detail {} ", key);
                SelenideElement input = getRootElement().$(String.format("input[name=\"%s\"", key)).shouldBe(visible);
                input.clear();
                input.sendKeys(data.get(key));
            } else {
                log.info("Input {} is not present on form!", key);
            }
        }
    }
}

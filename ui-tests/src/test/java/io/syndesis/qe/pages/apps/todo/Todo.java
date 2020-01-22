package io.syndesis.qe.pages.apps.todo;

import static org.assertj.core.api.Assertions.assertThat;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$$;

import org.openqa.selenium.By;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;

public class Todo {

    public void refresh() {
        Selenide.refresh();
    }

    public int getListItemsCount() {
        return getListItems().size();
    }

    public ElementsCollection getListItems() {
        return $$(By.xpath(".//li[@class='list-group-item']//input[@name='task' and @value!='']"));
    }

    public void setJmsForm(String text) {
        showJmsForm();
        SelenideElement textArea = $(By.name("message")).shouldBe(Condition.visible);
        textArea.clear();
        textArea.sendKeys(text);
    }

    public void sendJmsMessage() {
        showJmsForm();
        SelenideElement button = $(By.name("amq")).shouldBe(Condition.visible);
        button.click();
        assertThat($(By.xpath(".//*[@id=\"jmsToggle\"]/div/ul/li/div/span")).text()).contains("Message sent successfully");
    }

    public String getMessageFromTodo(int index) {
        ElementsCollection lists = getListItems();
        SelenideElement item = lists.get(index);
        return item.getValue();
    }

    public void showJmsForm() {
        SelenideElement button = $(By.id("toggleForm"));
        if (button.isDisplayed()) {
            button.click();
        }
    }
}

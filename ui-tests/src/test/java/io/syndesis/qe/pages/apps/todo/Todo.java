package io.syndesis.qe.pages.apps.todo;

import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$$;

import org.openqa.selenium.By;

public class Todo {

    public int getListItemsCount() {
        return $$(By.xpath("//li[@class='list-group-item']")).size();
    }
}

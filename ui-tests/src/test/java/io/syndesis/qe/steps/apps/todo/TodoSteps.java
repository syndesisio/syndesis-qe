package io.syndesis.qe.steps.apps.todo;

import static com.codeborne.selenide.Selenide.sleep;
import static com.codeborne.selenide.WebDriverRunner.getWebDriver;

import org.assertj.core.api.Assertions;

import com.codeborne.selenide.Selenide;

import cucumber.api.java.en.Then;
import io.syndesis.qe.pages.apps.todo.Todo;
import io.syndesis.qe.utils.OpenShiftUtils;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TodoSteps {
    private Todo todoPage = new Todo();

    @Then("^.* goes? to Todo app$")
    public void openTodoApp() {
        String url = "http://" + OpenShiftUtils.getInstance().getRoute("todo").getSpec().getHost();
        log.info("Opening Todo app on url: " + url);
        getWebDriver().navigate().to(url);
    }

    @Then("^.* checks? Todo list grows in \"([1-9][0-9]*)\" seconds?$")
    public void checkListGrows(int seconds) {
        log.info("Checking Todo app list is growing in " + seconds + " seconds");

        int startCount = todoPage.getListItemsCount();
        sleep(seconds*1000);
        Selenide.refresh();

        int endCount = todoPage.getListItemsCount();
        Assertions.assertThat(startCount).isLessThan(endCount);
    }
}

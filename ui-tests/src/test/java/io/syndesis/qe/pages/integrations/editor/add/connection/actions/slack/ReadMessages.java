package io.syndesis.qe.pages.integrations.editor.add.connection.actions.slack;

import static com.codeborne.selenide.Condition.exactText;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;

import io.syndesis.qe.fragments.common.form.Form;
import io.syndesis.qe.pages.SyndesisRootPage;

import org.openqa.selenium.By;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.SelenideElement;

import java.util.HashMap;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ReadMessages {

    private static final class Element {
        public static final By SELECT_DELAY = By.cssSelector("[data-testid='delay-duration']");
    }

    public void selectChannel(String channel) {
        //check that value of the channel is in the option data list
        SelenideElement dataList = $(By.id("channel-list"));
        dataList.findAll(By.tagName("option")).filter(Condition.value(channel)).shouldHaveSize(1);
        //the name of the channel has to be fill in manually. The select from the option in the <datalist> is not able
        Map<String, String> mymap = new HashMap<String, String>();
        mymap.put("channel", channel);
        new Form(new SyndesisRootPage().getRootElement()).fillByTestId(mymap);

    }

    public void selectDelayTimeUnits(String timeUnits) {
        SelenideElement selectElement = $(ReadMessages.Element.SELECT_DELAY).shouldBe(visible);
        selectElement.click();
        log.info(selectElement.toString());
        $(By.className("pf-c-dropdown__menu")).$$(By.tagName("li")).filter(exactText(timeUnits)).get(0).click();
    }
}

package io.syndesis.qe.steps;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$$;

import io.syndesis.qe.TestConfiguration;
import io.syndesis.qe.utils.OpenShiftUtils;

import org.junit.Assert;

import org.openqa.selenium.By;

import com.codeborne.selenide.CollectionCondition;
import com.codeborne.selenide.Condition;
import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.Selenide;

import java.util.Optional;

import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import io.fabric8.kubernetes.api.model.Pod;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MarketplaceSteps {

    @When("go to openshift main page")
    public static void goToOpenshift() {
        Selenide.open("https://console-openshift-console." + TestConfiguration.openShiftRouteSuffix());

        // and login using admin username and password
        $(By.partialLinkText("htpasswd")).shouldBe(Condition.visible).click();
        $(By.id("inputUsername")).shouldBe(Condition.visible).sendKeys(TestConfiguration.adminUsername());
        $(By.id("inputPassword")).shouldBe(Condition.visible).sendKeys(TestConfiguration.adminPassword());
        $(By.xpath(".//button[@type='submit']")).should(Condition.visible).click();
    }

    @When("open installed operators page")
    public void openInstalledOperators() {
        $(By.linkText("Operators")).shouldBe(Condition.visible).click();
        $(By.linkText("Installed Operators")).shouldBe(Condition.visible).click();
    }

    @When("select correct namespace in dropdown list")
    public static void selectNamespace() {
        $(By.cssSelector("div#content")).should(Condition.visible)
            .$(By.cssSelector("div.co-namespace-selector")).shouldBe(Condition.visible).click();

        $("div.dropdown-menu__filter input").shouldBe(Condition.visible)
            .sendKeys(TestConfiguration.openShiftNamespace());

        String namespaceCssSelector = "a#" + TestConfiguration.openShiftNamespace() + "-link";
        $(By.cssSelector(namespaceCssSelector)).shouldBe(Condition.visible).click();
    }

    @Then("check that operator \"([^\"]*)\" is in status \"([^\"]*)\"")
    public static void checkOperatorSucceeded(String operator, String status) {
        $(".ReactVirtualized__VirtualGrid").shouldBe(Condition.visible)
            .$$("tbody tr")
            .filter(Condition.visible)
            .shouldHave(CollectionCondition.sizeGreaterThan(0));

        ElementsCollection operators = $$(".ReactVirtualized__VirtualGrid tbody tr");
        operators.filter(Condition.text(operator)).shouldHave(CollectionCondition.texts(status));
    }

    @Then("check by api that pod with name \"([^\"]*)\" exists in namespace and is in phase \"([^\"]*)\"")
    public static void checkByApiPodStatus(String operatorPartialName, String phase) {
        Optional<Pod> pod = OpenShiftUtils.getPodByPartialName(operatorPartialName);

        Assert.assertTrue("Pod must exist", pod.isPresent());
        Assert.assertEquals("Pod must be in correct phase", pod.get().getStatus().getPhase(), phase);
    }
}

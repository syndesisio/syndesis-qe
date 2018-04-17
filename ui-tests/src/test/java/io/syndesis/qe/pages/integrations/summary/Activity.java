package io.syndesis.qe.pages.integrations.summary;

import static com.codeborne.selenide.CollectionCondition.size;
import static com.codeborne.selenide.CollectionCondition.sizeGreaterThan;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;

import org.assertj.core.api.Assertions;
import org.openqa.selenium.By;
import org.springframework.beans.factory.annotation.Autowired;

import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;

import java.util.List;
import java.util.Optional;

import io.fabric8.kubernetes.api.model.Pod;
import io.syndesis.common.model.integration.Integration;
import io.syndesis.qe.endpoints.ActivityIntegrationsEndpoint;
import io.syndesis.qe.endpoints.IntegrationsEndpoint;
import io.syndesis.qe.pages.SyndesisPageObject;
import io.syndesis.qe.utils.OpenShiftUtils;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Activity extends SyndesisPageObject {
    @Autowired
    private IntegrationsEndpoint integrationsEndpoint;
    @Autowired
    private ActivityIntegrationsEndpoint activityIntegrationsEndpoint;

    private static final class Element {
        public static final By ROOT = By.cssSelector("pfng-list");

        public static final By ACTIVITY_WRAPPED = By.className("list-pf-item");
        public static final By ACTIVITY_BASIC = By.className("list-pf-container");
        public static final By ACTIVITY_EXPANDED = By.className("pfng-list-expansion");
        public static final By ACTIVITY_EXPNADER = By.className("fa-angle-right");

        //TODO(sveres), these elements are not available yet.
        public static final By ACTIVITY_STEPS = By.cssSelector("TODO(sveres)activity detail B");
        public static final By ACTIVITY_STEP = By.cssSelector("TODO(sveres)activity detail C");
        public static final By ACTIVITY_STEP_NAME = By.cssSelector("TODO(sveres)activity detail C.2");
        public static final By ACTIVITY_LOG_VIEW = By.cssSelector("TODO(sveres)activity log D");
        public static final By CALENDAR = By.cssSelector("TODO(sveres)activity calendar E");
    }

    @Override
    public SelenideElement getRootElement() {
        SelenideElement elementRoot = $(Element.ROOT).shouldBe(visible);
        return elementRoot;
    }

    @Override
    public boolean validate() {
        return getRootElement().is(visible);
    }

    public void clickOnActivity(int order) {
        ElementsCollection activities = this.getAllIntegrationActivities().shouldBe(sizeGreaterThan(0));
        this.clickOnActivityExpander(activities.get(order));
    }

    public void clickButtonOfActivityStep(String buttonName, String stepName) {
        ElementsCollection activitySteps = $(Element.ROOT).find(Element.ACTIVITY_STEPS).shouldBe(visible).findAll(Element.ACTIVITY_STEP).shouldBe(sizeGreaterThan(0));
        SelenideElement step = activitySteps.stream().filter(el -> checkStepName(el, stepName)).findFirst().get();
        this.clickButtonInStep(step, buttonName);
    }

    public void checkLogIsPresent() {
        $(Element.ROOT).findAll(Element.ACTIVITY_LOG_VIEW).shouldBe(size(1));
    }

    public void checkLogIsValid(String integrationName, String activityStepName) {

        String text = $(Element.ROOT).findAll(Element.ACTIVITY_LOG_VIEW).shouldBe(size(1)).get(0).getText();

        String integrationId = this.getIdByIntegrationName(integrationName);
        Assertions.assertThat(integrationId).isNotNull();
        //1.
        List<io.syndesis.server.endpoint.v1.handler.activity.Activity> activityIntegrationLogs = activityIntegrationsEndpoint.list(integrationId);
        boolean result = this.checkActivityText(activityIntegrationLogs.stream().findFirst().get(), activityStepName, text);

        Assertions.assertThat(result).isEqualTo(true);

    }

    public void setCalendar(String startDate, String endDate) {
        SelenideElement calendar = $(Element.ROOT).find(Element.CALENDAR).shouldBe(visible);
        //TODO(sveres): how to select specific date in calendar component?
    }



//AUXILIARIES:

    private ElementsCollection getAllIntegrationActivities(){
        return $(Element.ROOT).findAll(Element.ACTIVITY_WRAPPED);
    }

    private void clickOnActivityExpander(SelenideElement activity){
        activity.find(Element.ACTIVITY_EXPANDED).shouldBe(visible).click();
    }

    private boolean checkStepName(SelenideElement activityStepElement, String stepName){
        return activityStepElement.find(Element.ACTIVITY_STEP_NAME).is(visible);
    }

    private void clickButtonInStep(SelenideElement activityStepElement, String buttonTitle){
        activityStepElement.find(By.cssSelector(String.format("button:contains('%s')", buttonTitle))).shouldBe(visible).click();
    }

    private String getIdByIntegrationName(String integrationName) {
        List<Integration> integrations = integrationsEndpoint.list();
        Integration integr = integrations.stream().filter(integration -> integrationName.equals(integration.getName())).findAny().orElse(null);
        return integr.getId().get();
    }

    private boolean checkActivityText(io.syndesis.server.endpoint.v1.handler.activity.Activity activity, String activityStepName, String expectedText) {

        String podName = activity.getPod();

        Optional<Pod> buildPod = OpenShiftUtils.getInstance().getPods().stream().filter(p -> p.getMetadata().getName().equals(podName)).findFirst();

        if (buildPod.isPresent()) {
            String logText = OpenShiftUtils.getInstance().getPodLog(buildPod.get());
            Assertions.assertThat(logText).isNotEmpty();
            //2. TODO(sveres)  question: there is table of activity _steps (ACTIVITY_STEPS) how is this mapped to Activities list of integration?
            // TODO(sveres) i.e. how could I utilize activityStepName?

            //3. TODO(sveres) compare texts
            return false;
        } else {
            Assertions.fail("No pod found for pod name: " + podName);
        }
        return false;
    }
}

package io.syndesis.qe.pages.integrations.edit;

import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;

import org.openqa.selenium.By;

import com.codeborne.selenide.SelenideElement;

import io.syndesis.qe.pages.SyndesisPageObject;
import io.syndesis.qe.pages.integrations.edit.actions.ActionConfigureComponent;
import io.syndesis.qe.pages.integrations.edit.actions.ActionConfigureComponentInvokeSql;
import io.syndesis.qe.pages.integrations.edit.actions.ActionConfigureComponentJmsPublish;
import io.syndesis.qe.pages.integrations.edit.actions.ActionConfigureComponentJmsRequest;
import io.syndesis.qe.pages.integrations.edit.actions.ActionConfigureComponentJmsSubscribe;
import io.syndesis.qe.pages.integrations.edit.actions.ActionConfigureComponentPeriodicSql;
import io.syndesis.qe.pages.integrations.edit.actions.ActionConfigureComponentTwitterSearch;
import io.syndesis.qe.pages.integrations.edit.steps.DataMapperComponent;
import io.syndesis.qe.pages.integrations.edit.steps.StepComponent;
import io.syndesis.qe.pages.integrations.edit.steps.StepComponentFactory;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class IntegrationEditPage extends SyndesisPageObject {

    private static final class Element {
        public static final By ROOT = By.cssSelector("syndesis-integration-edit-page");
    }
    @Getter
    private ActionConfigureComponent actionConfigureComponent = new ActionConfigureComponent();
    @Getter
    private ActionConfigureComponentPeriodicSql periodicSqlComponent = new ActionConfigureComponentPeriodicSql();
    @Getter
    private ActionConfigureComponentInvokeSql invokeSqlComponent = new ActionConfigureComponentInvokeSql();
    @Getter
    private ActionConfigureComponentJmsSubscribe jmsSubscribeComponent = new ActionConfigureComponentJmsSubscribe();
    @Getter
    private ActionConfigureComponentJmsPublish jmsPublishComponent = new ActionConfigureComponentJmsPublish();
    @Getter
    private ActionConfigureComponentJmsRequest jmsRequestComponent = new ActionConfigureComponentJmsRequest();
    @Getter
    private ActionConfigureComponentTwitterSearch twitterSearchComponent = new ActionConfigureComponentTwitterSearch();
    @Getter
    private ListActionsComponent listActionsComponent = new ListActionsComponent();
    @Getter
    private IntegrationFlowViewComponent flowViewComponent = new IntegrationFlowViewComponent();
    @Getter
    private IntegrationBasicsComponent integrationBasicsComponent = new IntegrationBasicsComponent();
    @Getter
    private IntegrationConnectionSelectComponent integrationConnectionSelectComponent = new IntegrationConnectionSelectComponent();
    @Getter
    private IntegrationStepSelectComponent integrationStepSelectComponent = new IntegrationStepSelectComponent();
    @Getter
    private DataMapperComponent dataMapperComponent = new DataMapperComponent();

    private StepComponentFactory stepComponentFactory = new StepComponentFactory();
    private StepComponent stepComponent;

    @Override
    public SelenideElement getRootElement() {
        SelenideElement elementRoot = $(Element.ROOT).shouldBe(visible);
        return elementRoot;
    }

    @Override
    public boolean validate() {
        return getRootElement().is(visible);
    }

    public StepComponent getStepComponent(String stepType, String parameter) {
        stepComponent = stepComponentFactory.getStep(stepType, parameter);
        return stepComponent;
    }

}

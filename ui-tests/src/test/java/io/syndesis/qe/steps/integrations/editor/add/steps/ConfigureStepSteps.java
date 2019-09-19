package io.syndesis.qe.steps.integrations.editor.add.steps;

import static org.junit.Assert.assertThat;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.notNullValue;

import static com.codeborne.selenide.Condition.visible;

import io.syndesis.qe.pages.integrations.editor.add.connection.actions.database.PeriodicSql;
import io.syndesis.qe.pages.integrations.editor.add.connection.actions.ftp.FtpDataType;
import io.syndesis.qe.pages.integrations.editor.add.connection.actions.ftp.FtpDownload;
import io.syndesis.qe.pages.integrations.editor.add.connection.actions.ftp.FtpUpload;
import io.syndesis.qe.pages.integrations.editor.add.connection.actions.jms.JmsPublish;
import io.syndesis.qe.pages.integrations.editor.add.connection.actions.jms.JmsSubscribe;
import io.syndesis.qe.pages.integrations.editor.add.connection.actions.slack.ReadMessages;
import io.syndesis.qe.pages.integrations.editor.add.connection.actions.twitter.TwitterSearch;
import io.syndesis.qe.pages.integrations.editor.add.steps.BasicFilter;
import io.syndesis.qe.pages.integrations.editor.add.steps.getridof.AbstractStep;
import io.syndesis.qe.pages.integrations.editor.add.steps.getridof.StepFactory;
import io.syndesis.qe.pages.integrations.fragments.IntegrationFlowView;

import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;

import java.util.List;
import java.util.Map;

import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import io.cucumber.datatable.DataTable;
import io.fabric8.kubernetes.client.utils.Utils;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ConfigureStepSteps {

    private IntegrationFlowView integrationFlowView = new IntegrationFlowView();
    private TwitterSearch twitterSearch = new TwitterSearch();
    private JmsSubscribe jmsSubscribe = new JmsSubscribe();
    private JmsPublish jmsPublish = new JmsPublish();
    private FtpDownload ftpDownload = new FtpDownload();
    private FtpDataType ftpDataType = new FtpDataType();
    private FtpUpload ftpUpload = new FtpUpload();
    private PeriodicSql periodicSql = new PeriodicSql();
    private ReadMessages slackReadMessages = new ReadMessages();

    @Then("^check visibility of \"([^\"]*)\" step configuration page$")
    public void verifyConfigureStepPage(String stepType) {
        AbstractStep stepComponent = StepFactory.getStep(stepType, "");
        log.info("there must be add step editPage root element");
        stepComponent.getRootElement().shouldBe(visible);
        assertThat(stepComponent.validate()).isTrue();
    }

    @When("^fill in the configuration page for \"([^\"]*)\" step with \"([^\"]*)\" parameter$")
    public void fillStepConfiguration(String stepType, String parameter) {
        AbstractStep stepComponent = StepFactory.getStep(stepType, parameter);
        stepComponent.fillConfiguration();
    }

    @Then("^check that basic filter step path input options contains \"([^\"]*)\" option$")
    public void checkBasicFilterStepOption(String option) {
        BasicFilter basicFilterStepComponent = new BasicFilter("");
        List<String> options = basicFilterStepComponent.getPathInputOptions();

        assertThat(options.contains(option)).isTrue();
    }

    @When("^add new basic filter rule with \"([^\"]*)\" parameters$")
    public void addBasicFilterRule(String rule) {
        BasicFilter basicFilterStepPage = (BasicFilter) StepFactory.getStep("BASIC FILTER", "");
        basicFilterStepPage.initialize();
        basicFilterStepPage.addRule(rule);
    }

    @When("^delete \"(\\d+)\" random basic filter rule$")
    public void deleteRandomFilterRules(Integer numberOfRules) {
        for (int i = 0; i < numberOfRules; i++) {
            integrationFlowView.clickRandomTrash();
        }
    }

    @When("^delete basic filter rule on position \"(\\d+)\"$")
    public void deleteFilterRuleOnPosition(Integer position) {
        ElementsCollection trashes = integrationFlowView.getAllTrashes();
        trashes.get(position - 1).click();
    }

    // Twitter search specification
    @When("^fill in keywords field with random text to configure search action$")
    public void fillKeywordsToTwitterSearch() {
        String value = Utils.randomString(20);
        twitterSearch.fillInput(value);
    }

    @When("^sets jms subscribe inputs source data$")
    public void setJmsSubscribeData(DataTable sourceMappingData) {
        List<Map<String, String>> maps = sourceMappingData.asMaps(String.class, String.class);
        for (Map<String, String> source : maps) {
            for (String field : source.keySet()) {
                SelenideElement element = jmsSubscribe.checkAndGetFieldTypeById(field);
                assertThat(element, notNullValue());
                jmsSubscribe.setElementValue(source.get(field), element);
            }
        }
    }

    @When("^sets jms request inputs source data$")
    public void setJmsRequestData(DataTable sourceMappingData) {
        List<Map<String, String>> maps = sourceMappingData.asMaps(String.class, String.class);
        for (Map<String, String> source : maps) {
            for (String field : source.keySet()) {
                //shouldn't be here jmsRequest class used??
                SelenideElement element = jmsSubscribe.checkAndGetFieldTypeById(field);
                assertThat(element, notNullValue());
                jmsSubscribe.setElementValue(source.get(field), element);
            }
        }
    }

    @When("^sets jms publish inputs source data$")
    public void setJmsPublishData(DataTable sourceMappingData) {
        List<Map<String, String>> maps = sourceMappingData.asMaps(String.class, String.class);
        for (Map<String, String> source : maps) {
            for (String field : source.keySet()) {
                SelenideElement element = jmsPublish.checkAndGetFieldTypeById(field);
                assertThat(element, notNullValue());
                jmsPublish.setElementValue(source.get(field), element);
            }
        }
    }

    @When("^fill in ftp download form with values$")
    public void setFtpDownloadData(DataTable sourceMappingData) {
        List<Map<String, String>> maps = sourceMappingData.asMaps(String.class, String.class);
        for (Map<String, String> source : maps) {
            for (String field : source.keySet()) {
                SelenideElement element = ftpDownload.checkAndGetFieldTypeByName(field);
                assertThat(element, notNullValue());
                ftpDownload.setElementValue(source.get(field), element);
            }
        }
    }

    @When("^fill in specify output data type form with values$")
    public void setOutputDataTypeData(DataTable sourceMappingData) {
        List<Map<String, String>> maps = sourceMappingData.asMaps(String.class, String.class);
        for (Map<String, String> source : maps) {
            for (String field : source.keySet()) {
                SelenideElement element = ftpDataType.checkAndGetFieldTypeByName(field);
                assertThat(element, notNullValue());
                ftpDataType.setElementValue(source.get(field), element);
            }
        }
    }

    @When("^fill in ftp upload form with values$")
    public void setFtpUploadData(DataTable sourceMappingData) {
        List<Map<String, String>> maps = sourceMappingData.asMaps(String.class, String.class);
        for (Map<String, String> source : maps) {
            for (String field : source.keySet()) {
                SelenideElement element = ftpUpload.checkAndGetFieldTypeByName(field);
                assertThat(element, notNullValue());
                ftpUpload.setElementValue(source.get(field), element);
            }
        }
    }

    @When("^select \"([^\"]*)\" from sql dropdown$")
    public void selectsFromDropdownByClassName(String timeUnits) {
        periodicSql.selectSQLperiodUnits(timeUnits);
    }

    @When("^select \"([^\"]*)\" from slack channel dropdown$")
    public void selectsFromSlackDropdownByChannelName(String channel) {
        slackReadMessages.selectChannel(channel);
    }

    @When("^select \"([^\"]*)\" from slack delay time units dropdown$")
    public void selectsFromSlackTimeUnitDropdown(String timeUnits) {
        slackReadMessages.selectDelayTimeUnits(timeUnits);
    }
}

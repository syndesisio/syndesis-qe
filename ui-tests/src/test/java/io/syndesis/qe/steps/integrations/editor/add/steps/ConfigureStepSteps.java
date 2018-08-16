package io.syndesis.qe.steps.integrations.editor.add.steps;

import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;
import cucumber.api.DataTable;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Then;
import io.fabric8.kubernetes.client.utils.Utils;
import io.syndesis.qe.pages.integrations.editor.add.connection.actions.database.PeriodicSql;
import io.syndesis.qe.pages.integrations.editor.add.connection.actions.ftp.FtpDataType;
import io.syndesis.qe.pages.integrations.editor.add.connection.actions.ftp.FtpDownload;
import io.syndesis.qe.pages.integrations.editor.add.connection.actions.ftp.FtpUpload;
import io.syndesis.qe.pages.integrations.editor.add.connection.actions.jms.JmsPublish;
import io.syndesis.qe.pages.integrations.editor.add.connection.actions.jms.JmsSubscribe;
import io.syndesis.qe.pages.integrations.editor.add.connection.actions.twitter.TwitterSearch;
import io.syndesis.qe.pages.integrations.editor.add.steps.BasicFilter;
import io.syndesis.qe.pages.integrations.editor.add.steps.getridof.AbstractStep;
import io.syndesis.qe.pages.integrations.editor.add.steps.getridof.StepFactory;
import io.syndesis.qe.pages.integrations.fragments.IntegrationFlowView;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

import static com.codeborne.selenide.Condition.visible;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

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

    @Then("^check visibility of \"([^\"]*)\" step configuration page$")
    public void verifyConfigureStepPage(String stepType) {
        AbstractStep stepComponent = StepFactory.getStep(stepType, "");
        log.info("there must be add step editPage root element");
        stepComponent.getRootElement().shouldBe(visible);
        assertThat(stepComponent.validate()).isTrue();
    }

    @Then("^fill in the configuration page for \"([^\"]*)\" step with \"([^\"]*)\" parameter$")
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

    @Then("^add new basic filter rule with \"([^\"]*)\" parameters$")
    public void addBasicFilterRule(String rule) {
        BasicFilter basicFilterStepPage = (BasicFilter) StepFactory.getStep("BASIC FILTER", "");
        basicFilterStepPage.initialize();
        basicFilterStepPage.addRule(rule);
    }

    @Then("^delete \"(\\d+)\" random basic filter rule$")
    public void deleteRandomFilterRules(Integer numberOfRules) {
        for (int i = 0; i < numberOfRules; i++) {
            integrationFlowView.clickRandomTrash();
        }
    }

    @Then("^delete basic filter rule on position \"(\\d+)\"$")
    public void deleteFilterRuleOnPosition(Integer position) {
        ElementsCollection trashes = integrationFlowView.getAllTrashes();
        trashes.get(position - 1).click();
    }

    // Twitter search specification
    @Then("^fill in keywords field with random text to configure search action$")
    public void fillKeywordsToTwitterSearch() {
        String value = Utils.randomString(20);
        twitterSearch.fillInput(value);
    }

    @And("^sets jms subscribe inputs source data$")
    public void setJmsSubscribeData(DataTable sourceMappingData) {
        for (Map<String, String> source : sourceMappingData.asMaps(String.class, String.class)) {
            for (String field : source.keySet()) {
                SelenideElement element = jmsSubscribe.checkAndGetFieldTypeById(field);
                assertThat(element, notNullValue());
                jmsSubscribe.setElementValue(source.get(field), element);
            }
        }
    }

    @And("^sets jms request inputs source data$")
    public void setJmsRequestData(DataTable sourceMappingData) {
        for (Map<String, String> source : sourceMappingData.asMaps(String.class, String.class)) {
            for (String field : source.keySet()) {
                //shouldn't be here jmsRequest class used??
                SelenideElement element = jmsSubscribe.checkAndGetFieldTypeById(field);
                assertThat(element, notNullValue());
                jmsSubscribe.setElementValue(source.get(field), element);
            }
        }
    }

    @And("^sets jms publish inputs source data$")
    public void setJmsPublishData(DataTable sourceMappingData) {
        for (Map<String, String> source : sourceMappingData.asMaps(String.class, String.class)) {
            for (String field : source.keySet()) {
                SelenideElement element = jmsPublish.checkAndGetFieldTypeById(field);
                assertThat(element, notNullValue());
                jmsPublish.setElementValue(source.get(field), element);
            }
        }
    }

    @And("^fill in ftp download form with values$")
    public void setFtpDownloadData(DataTable sourceMappingData) {
        for (Map<String, String> source : sourceMappingData.asMaps(String.class, String.class)) {
            for (String field : source.keySet()) {
                SelenideElement element = ftpDownload.checkAndGetFieldTypeByName(field);
                assertThat(element, notNullValue());
                ftpDownload.setElementValue(source.get(field), element);
            }
        }
    }

    @And("^fill in specify output data type form with values$")
    public void setOutputDataTypeData(DataTable sourceMappingData) {
        for (Map<String, String> source : sourceMappingData.asMaps(String.class, String.class)) {
            for (String field : source.keySet()) {
                SelenideElement element = ftpDataType.checkAndGetFieldTypeByName(field);
                assertThat(element, notNullValue());
                ftpDataType.setElementValue(source.get(field), element);
            }
        }
    }

    @And("^fill in ftp upload form with values$")
    public void setFtpUploadData(DataTable sourceMappingData) {
        for (Map<String, String> source : sourceMappingData.asMaps(String.class, String.class)) {
            for (String field : source.keySet()) {
                SelenideElement element = ftpUpload.checkAndGetFieldTypeByName(field);
                assertThat(element, notNullValue());
                ftpUpload.setElementValue(source.get(field), element);
            }
        }
    }

    @Then("^select \"([^\"]*)\" from sql dropdown$")
    public void selectsFromDopdownByClassName(String timeUnits) {
        periodicSql.selectSQLperiodUnits(timeUnits);
    }
}

package io.syndesis.qe.cucumber;

import io.syndesis.qe.test.InfraFail;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

import io.cucumber.plugin.EventListener;
import io.cucumber.plugin.event.EmbedEvent;
import io.cucumber.plugin.event.EventPublisher;
import io.cucumber.plugin.event.PickleStepTestStep;
import io.cucumber.plugin.event.TestCaseFinished;
import io.cucumber.plugin.event.TestCaseStarted;
import io.cucumber.plugin.event.TestStepFinished;
import io.cucumber.plugin.event.TestStepStarted;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ReportPortalFormatter implements EventListener {

    private final FeaturesInformationStore featuresInformationStore = FeaturesInformationStore.getInstance();
    private final String path;

    private final StringBuffer testCaseLog = new StringBuffer();
    private byte[] image;
    private boolean scenarioAlreadyFailed;

    @Override
    public void setEventPublisher(EventPublisher eventPublisher) {
        eventPublisher.registerHandlerFor(TestCaseStarted.class, this::onTestCaseStarted);
        eventPublisher.registerHandlerFor(TestStepStarted.class, this::onTestStepStarted);
        eventPublisher.registerHandlerFor(EmbedEvent.class, this::onEmbedEvent);
        eventPublisher.registerHandlerFor(TestStepFinished.class, this::onTestStepFinished);
        eventPublisher.registerHandlerFor(TestCaseFinished.class, this::onTestCaseFinished);
    }

    public ReportPortalFormatter(String path) {
        this.path = path;
    }

    /**
     * Clear log and image for the next test case (scenario)
     */
    private void onTestCaseStarted(TestCaseStarted t) {
        testCaseLog.delete(0, testCaseLog.length());
        image = null;
        scenarioAlreadyFailed = false;
    }

    /**
     * Print a step name to the log before each step starts
     */
    private void onTestStepStarted(TestStepStarted t) {
        if ((!scenarioAlreadyFailed) && t.getTestStep() instanceof PickleStepTestStep) {
            PickleStepTestStep testStep = (PickleStepTestStep) t.getTestStep();
            testCaseLog.append("STEP: ").append(testStep.getStep().getKeyword()).append(testStep.getStep().getText()).append("\n");
        }
    }

    /**
     * If during the test case (scenario) execution is fired an event with log/img to the cucumber bus (scenario.attach(..)), add them to the our
     * attachments too
     */
    private void onEmbedEvent(EmbedEvent event) {
        if ("text/plain".equals(event.getMediaType())) {
            testCaseLog.append("====").append(event.getName()).append("====").append("\n");
            testCaseLog.append(new String(event.getData())).append("\n");
        }
        if ("image/png".equals(event.getMediaType())) {
            image = event.getData();
        }
    }

    /**
     * After each steps, save all log events from events stack to the our test case (scenario) and clear the stack
     */
    private void onTestStepFinished(TestStepFinished t) {
        if (!t.getResult().getStatus().isOk()) {
            // it is not needed to print the other steps to the console for the failed test case (scenario)
            scenarioAlreadyFailed = true;
        }
        for (String event : LogInterceptor.getEventsStack()) {
            testCaseLog.append(event).append("\n");
        }
        LogInterceptor.getEventsStack().clear();
    }

    /**
     * When the test case (scenario) finished, save log and image to the attachments for the particular test case (scenario).
     * So every failed scenario will have log file and screenshot separately from each other in the folder with name <feature-name>.<scenario-name>
     */
    private void onTestCaseFinished(TestCaseFinished t) {
        if (!t.getResult().getStatus().isOk()) {
            //create folder in format <test case classname>.<test case name> and save there screenshot and log file
            String pathForThisTestCase =
                path + "/" + featuresInformationStore.getFeatures().get(t.getTestCase().getUri()).getName() + "." + t.getTestCase().getName();
            File directory = new File(pathForThisTestCase);
            if (directory.exists()) {
                InfraFail.fail("Directory with path " + pathForThisTestCase +
                    " exist! This evokes that feature file contains more scenario with the same name. This is not allowed due to ReportPortal " +
                    "reporting. Please rename the scenario!");
            } else {
                directory.mkdirs();
            }

            //save useful logs and image
            try {
                FileUtils.writeStringToFile(new File(pathForThisTestCase, "testScenario.log"), testCaseLog.toString(), "UTF-8");
            } catch (IOException e) {
                log.error("Error writing log", e);
            }

            if (image != null) {
                try {
                    FileUtils.writeByteArrayToFile(new File(pathForThisTestCase, "image.png"), image);
                } catch (IOException e) {
                    log.error("Error writing image", e);
                }
            }
        }
    }
}

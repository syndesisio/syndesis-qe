package io.syndesis.qe.storage;

import io.syndesis.common.model.integration.Step;
import io.syndesis.qe.entities.StepDefinition;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

/**
 * Jan 12, 2018 Red Hat
 *
 * @author tplevko@redhat.com
 */
@Slf4j
@Component
public class StepsStorage {

    private List<StepDefinition> stepDefinitions = null;

    public StepsStorage() {
        stepDefinitions = new ArrayList<>();
    }

    public List<StepDefinition> getStepDefinitions() {
        return stepDefinitions;
    }

    public List<Step> getSteps() {
        return stepDefinitions.stream().map(a -> a.getStep()).collect(Collectors.toList());
    }

    public StepDefinition getLastStepDefinition() {
        return stepDefinitions.get(stepDefinitions.size() - 1);
    }

    public void flushStepDefinitions() {
        stepDefinitions.clear();
    }
}

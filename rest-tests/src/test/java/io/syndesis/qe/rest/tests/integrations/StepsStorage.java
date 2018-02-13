package io.syndesis.qe.rest.tests.integrations;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import io.syndesis.model.integration.Step;
import lombok.extern.slf4j.Slf4j;

/**
 * Jan 12, 2018 Red Hat
 *
 * @author tplevko@redhat.com
 */
@Component
@Slf4j
public class StepsStorage {

    private List<Step> steps = null;

    public StepsStorage() {
        steps = new ArrayList<>();
    }

    public List<Step> getSteps() {
        return steps;
    }

    public void flushSteps() {
        steps.clear();
    }
}

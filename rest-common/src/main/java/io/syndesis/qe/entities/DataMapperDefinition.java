package io.syndesis.qe.entities;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

/**
 * Feb 21, 2018 Red Hat
 *
 * @author tplevko@redhat.com
 */
@Data
public class DataMapperDefinition {

    private List<String> source;

    private String target;

    private List<DataMapperStepDefinition> dataMapperStepDefinition;

    public DataMapperDefinition() {
        dataMapperStepDefinition = new ArrayList<>();
    }

    /**
     * Gets the last element.
     *
     * @return last element
     */
    public DataMapperStepDefinition getLastDatamapperStepDefinition() {
        return dataMapperStepDefinition.get(dataMapperStepDefinition.size() - 1);
    }
}

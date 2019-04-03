package io.syndesis.qe.bdd.entities;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.atlasmap.v2.MappingType;
import lombok.Data;

/**
 * Feb 21, 2018 Red Hat
 *
 * @author tplevko@redhat.com
 */
@Data
public class DataMapperStepDefinition {

    private MappingType MappingType;

    private int fromStep;

    private List<String> inputFields;

    private List<String> outputFields;

    private SeparatorType strategy;

    // Outer map keys: "source", "target"
    // Inner map keys: id of the field
    // Inner map values: transformations
    private Map<String, Map<String, List<Object>>> transformations = new HashMap<>();
}

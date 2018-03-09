package io.syndesis.qe.rest.tests.entities;

import java.util.List;

import io.atlasmap.v2.MappingType;
import lombok.Data;

/**
 * Feb 21, 2018 Red Hat
 *
 * @author tplevko@redhat.com
 */
@Data
public class DataMapperStepDefinition {

    MappingType MappingType;

    int fromStep;

    List<String> inputFields;

    List<String> outputFields;

    SeparatorType strategy;
}

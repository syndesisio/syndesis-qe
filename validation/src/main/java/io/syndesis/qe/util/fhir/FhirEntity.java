package io.syndesis.qe.util.fhir;

import lombok.Getter;
import lombok.Setter;

public enum FhirEntity {

    PATIENT("Patient"),
    BASIC("Basic");

    @Setter
    @Getter
    private String name;

    FhirEntity(String name) {
        this.name = name;
    }
}

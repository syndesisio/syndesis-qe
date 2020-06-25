package io.syndesis.qe.util.fhir;

import lombok.Getter;
import lombok.Setter;

@Getter
public class MyPatientSpecification {
    private final String familyName;
    private final String givenName;
    @Setter
    private String id;

    public MyPatientSpecification(String fn, String gn) {
        familyName = fn;
        givenName = gn;
    }

    public MyPatientSpecification(String fn, String gn, String id) {
        familyName = fn;
        givenName = gn;
        this.id = id;
    }

    @Override
    public String toString() {
        return String.format("%s %s id:%s", familyName, givenName, id);
    }
}

package io.syndesis.qe.util.fhir;

import lombok.Getter;
import lombok.Setter;

@Getter
public class MyBasicSpecification {
    private final String language;
    @Setter
    private String id;

    public MyBasicSpecification(String lang) {
        language = lang;
    }

    public MyBasicSpecification(String lang, String id) {
        this.language = lang;
        this.id = id;
    }

    @Override
    public String toString() {
        return String.format("Basic: %s id:%s", language, id);
    }
}

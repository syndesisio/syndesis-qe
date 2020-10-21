package io.syndesis.qe.cucumber;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import io.cucumber.messages.Messages;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * Storage for information usage in Formatters. The storage are fill up in onTestSourceRead method.
 */
@Slf4j
public class FeaturesInformationStore {

    private static FeaturesInformationStore instance;

    private FeaturesInformationStore() {
    }

    public static FeaturesInformationStore getInstance() {
        if (instance == null) {
            instance = new FeaturesInformationStore();
        }
        return instance;
    }

    @Getter
    private final Map<URI, Messages.GherkinDocument.Feature> features = new HashMap<>();

    @Getter
    private final Map<URI, String> sustainers = new HashMap<>();
}

package io.syndesis.qe.rest.tests.steps.extension;

import static org.assertj.core.api.Assertions.assertThat;

import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.util.List;

import cucumber.api.java.en.Given;
import io.cucumber.datatable.DataTable;
import io.syndesis.common.model.extension.Extension;
import io.syndesis.qe.endpoints.ExtensionsEndpoint;

public class ExtensionHandler {
    @Autowired
    private ExtensionsEndpoint extensionsEndpoint;

    @Given("^import extensions from syndesis-extensions folder$")
    public void importExtensionsFromSyndesisExtensionsFolder(DataTable properties) {
        List<String> extensions = properties.asList(String.class);
        for (String ext : extensions) {
            String defaultPath = "../syndesis-extensions/" + ext + "/target/";
            File[] files = new File(defaultPath).listFiles((dir, name) -> !name.contains("original") && name.endsWith(".jar"));
            assertThat(files).hasSize(1).doesNotContainNull();
            Extension e = extensionsEndpoint.uploadExtension(files[0]);
            extensionsEndpoint.installExtension(e);
        }
    }
}

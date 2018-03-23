package io.syndesis.qe.utils;

import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.json.JSONObject;
import org.junit.Assert;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

@Slf4j
public class ExportedIntegrationJSONUtil {
    public static void testExportedFile(File file) {
        try (ZipFile zipFile = new ZipFile(file)) {

            Enumeration<? extends ZipEntry> entries = zipFile.entries();

            while (entries.hasMoreElements()) {
                ZipEntry zipEntry = entries.nextElement();
                if (zipEntry.getName().equalsIgnoreCase("model.json")) {
                    doTestExportedFile(zipFile, zipEntry);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            Assert.fail("Error while processing exported integration zip file");
        }
    }

    private static void doTestExportedFile(ZipFile file, ZipEntry entry) {
        try (InputStream is = file.getInputStream(entry)) {
            List<String> doc = new BufferedReader(new InputStreamReader(is,
                    StandardCharsets.UTF_8)).lines().collect(Collectors.toList());

            JSONObject jsonObj = new JSONObject(String.join("\n", doc));
            JSONObject connections = jsonObj.getJSONObject("connections");

            //I don't know the hash name of connection, have to iterate and get it
            Iterator<String> keys = connections.keys();
            String currentConnectionType;
            JSONObject currentConnectionJson;
            String keyName;

            while (keys.hasNext()) {
                keyName = keys.next();
                currentConnectionJson = connections.getJSONObject(keyName);
                currentConnectionType = currentConnectionJson.getString("connectorId");

                if (currentConnectionType.equalsIgnoreCase("twitter")) {
                    testTwitterEncryption(currentConnectionJson);
                } else if (currentConnectionType.equalsIgnoreCase("salesforce")) {
                    testSalesforceEncryption(currentConnectionJson);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            Assert.fail("Error while processing exported integration zip entry");
        }
    }

    private static void testSalesforceEncryption(JSONObject json) {
        log.info("Checking salesforce credentials encryption...");
        JSONObject configuredProperties = json.getJSONObject("configuredProperties");

        Assertions.assertThat(configuredProperties.getString("clientSecret"))
                .isNotEmpty()
                .containsIgnoringCase("»ENC:");

        Assertions.assertThat(configuredProperties.getString("password"))
                .isNotEmpty()
                .containsIgnoringCase("»ENC:");

        Assertions.assertThat(configuredProperties.getString("clientId"))
                .isNotEmpty()
                .containsIgnoringCase("»ENC:");
    }

    private static void testTwitterEncryption(JSONObject json) {
        log.info("Checking twitter credentials encryption...");

        //get encrypted part
        JSONObject configuredProperties = json.getJSONObject("configuredProperties");
        //iterate over it
        Iterator<String> keys = configuredProperties.keys();

        String keyName;

        while (keys.hasNext()) {
            keyName = keys.next();
            Assertions.assertThat(configuredProperties.getString(keyName))
                    .isNotEmpty()
                    .containsIgnoringCase("»ENC:");
        }
    }
}

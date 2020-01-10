package io.syndesis.qe.resource.impl;

import static org.assertj.core.api.Assertions.fail;

import io.syndesis.qe.TestConfiguration;
import io.syndesis.qe.endpoints.TestSupport;
import io.syndesis.qe.resource.Resource;
import io.syndesis.qe.utils.OpenShiftUtils;
import io.syndesis.qe.wait.OpenShiftWaitUtils;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.io.FileUtils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CamelK implements Resource {
    private static final String CAMEL_K_ARCHIVE_PATH = String.format(
        "https://github.com/apache/camel-k/releases/download/%s/camel-k-client-%s-%s-64bit.tar.gz",
        TestConfiguration.camelKVersion(),
        TestConfiguration.camelKVersion(),
        System.getProperty("os.name").toLowerCase().contains("mac") ? "mac" : "linux"
    );
    private static final String LOCAL_ARCHIVE_PATH = "/tmp/camelk.tar.gz";
    private static final String LOCAL_ARCHIVE_EXTRACT_DIRECTORY = "/tmp/camelk";

    @Override
    public void deploy() {
        downloadArchive();
        extractArchive();
        installViaBinary();
    }

    @Override
    public void undeploy() {
        // It is needed to reset-db first, otherwise server would create the integrations again
        TestSupport.getInstance().resetDB();
        removeViaBinary();
    }

    @Override
    public boolean isReady() {
        return OpenShiftWaitUtils.isPodReady(OpenShiftUtils.getAnyPod("name", "camel-k-operator"));
    }

    private void downloadArchive() {
        try {
            FileUtils.copyURLToFile(new URL(CAMEL_K_ARCHIVE_PATH), new File(LOCAL_ARCHIVE_PATH));
        } catch (IOException e) {
            fail("Unable to download file " + CAMEL_K_ARCHIVE_PATH, e);
        }
    }

    private void extractArchive() {
        Path input = Paths.get(LOCAL_ARCHIVE_PATH);
        Path output = Paths.get(LOCAL_ARCHIVE_EXTRACT_DIRECTORY);

        try {
            FileUtils.deleteDirectory(new File(LOCAL_ARCHIVE_EXTRACT_DIRECTORY));
        } catch (IOException e) {
            e.printStackTrace();
        }
        output.toFile().mkdirs();

        try (TarArchiveInputStream tais = new TarArchiveInputStream(
            new GzipCompressorInputStream(new BufferedInputStream(Files.newInputStream(input))))) {
            ArchiveEntry archiveentry = null;
            while ((archiveentry = tais.getNextEntry()) != null) {
                Path pathEntryOutput = output.resolve(archiveentry.getName());
                if (archiveentry.isDirectory()) {
                    if (!Files.exists(pathEntryOutput)) {
                        Files.createDirectory(pathEntryOutput);
                    }
                } else {
                    Files.copy(tais, pathEntryOutput);
                }
            }
        } catch (IOException e) {
            fail("Unable to extract " + LOCAL_ARCHIVE_PATH, e);
        }
    }

    private void installViaBinary() {
        try {
            new File(LOCAL_ARCHIVE_EXTRACT_DIRECTORY + "/kamel").setExecutable(true);
            Runtime.getRuntime().exec(LOCAL_ARCHIVE_EXTRACT_DIRECTORY + "/kamel install").waitFor();
        } catch (Exception e) {
            fail("Unable to invoke kamel binary", e);
        }
    }

    private void removeViaBinary() {
        try {
            new File(LOCAL_ARCHIVE_EXTRACT_DIRECTORY + "/kamel").setExecutable(true);
            Runtime.getRuntime().exec(LOCAL_ARCHIVE_EXTRACT_DIRECTORY + "/kamel reset").waitFor();
        } catch (Exception e) {
            fail("Unable to invoke kamel binary", e);
        }
    }
}

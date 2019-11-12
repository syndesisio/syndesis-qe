package io.syndesis.qe.camelk;

import static org.assertj.core.api.Fail.fail;

import io.syndesis.qe.TestConfiguration;
import io.syndesis.qe.endpoints.TestSupport;
import io.syndesis.qe.utils.OpenShiftUtils;
import io.syndesis.qe.wait.OpenShiftWaitUtils;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.io.FileUtils;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CamelKTemplate {

    @Data
    private static final class CamelKConfig {
        public final String enabled;
    }

    @Data
    public static final class CamelKIntegration {
        public final String id;
        public final String phase;
        public final String context;
    }

    public static void editSpec(Map<String, Object> spec) {
        Map<String, Object> addons = (Map<String, Object>) spec.computeIfAbsent("addons", s -> new HashMap<String, Object>());
        addons.put("camelk", new CamelKConfig("true"));
    }

    public static void deployCamelKOperator() {
        ProcessBuilder pb = new ProcessBuilder("docker", "pull", TestConfiguration.getCamelKOperator());

        try {
            pb.start().waitFor();
        } catch (IOException | InterruptedException e) {
            log.error("Unable to pull Camel K operator \"{}\"", TestConfiguration.getCamelKOperator(), e);
        }

        downloadArchive();
        extractArchive();
        installViaBinary();
    }

    private static final String CAMEL_K_ARCHIVE_PATH = String.format(
        "https://github.com/apache/camel-k/releases/download/%s/camel-k-client-%s-%s-64bit.tar.gz",
        "0.3.4",
        "0.3.4",
        System.getProperty("os.name").toLowerCase().contains("mac") ? "mac" : "linux"
    );
    private static final String LOCAL_ARCHIVE_PATH = "/tmp/camelk.tar.gz";
    private static final String LOCAL_ARCHIVE_EXTRACT_DIRECTORY = "/tmp/camelk";

    public static void deploy() {
        downloadArchive();
        extractArchive();
        installViaBinary();
        OpenShiftUtils.binary().execute("secrets", "link", "camel-k-operator", "syndesis-pull-secret", "--for=pull");
        OpenShiftUtils.binary().execute("delete", "pod", "-l", "name=camel-k-operator");
        OpenShiftWaitUtils.waitUntilPodIsRunning("camel-k-operator");
    }

    public static void undeploy() {
        // It is needed to reset-db first, otherwise server would create the integrations again
        TestSupport.getInstance().resetDB();
        removeViaBinary();
    }

    private static void downloadArchive() {
        try {
            FileUtils.copyURLToFile(new URL(CAMEL_K_ARCHIVE_PATH), new File(LOCAL_ARCHIVE_PATH));
        } catch (IOException e) {
            fail("Unable to download file " + CAMEL_K_ARCHIVE_PATH, e);
        }
    }

    private static void extractArchive() {
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

    public static Map<String, CamelKIntegration> getCamelKIntegrations() {
        try {
            new File(LOCAL_ARCHIVE_EXTRACT_DIRECTORY + "/kamel").setExecutable(true);
            ProcessBuilder pb = new ProcessBuilder(LOCAL_ARCHIVE_EXTRACT_DIRECTORY + "/kamel", "get");
            Process p = pb.start();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
                return reader.lines()
                    .map(line -> line.replaceAll("\\s+", "\t").split("\t"))
                    .filter(parts -> parts.length == 3 && !"NAME".equals(parts[0]))
                    .map(parts -> new CamelKIntegration(parts[0], parts[1], parts[2]))
                    .collect(Collectors.toMap(CamelKIntegration::getId, Function.identity()));
            }
        } catch (Exception e) {
            fail("Failed to list Camel K integrations", e);
        }
        return new HashMap<>();
    }

    private static void installViaBinary() {
        try {
            new File(LOCAL_ARCHIVE_EXTRACT_DIRECTORY + "/kamel").setExecutable(true);
            Runtime.getRuntime().exec(LOCAL_ARCHIVE_EXTRACT_DIRECTORY + "/kamel install --operator-image " + TestConfiguration.getCamelKOperator())
                .waitFor();
        } catch (Exception e) {
            fail("Unable to invoke kamel binary", e);
        }
    }

    private static void removeViaBinary() {
        try {
            new File(LOCAL_ARCHIVE_EXTRACT_DIRECTORY + "/kamel").setExecutable(true);
            Runtime.getRuntime().exec(LOCAL_ARCHIVE_EXTRACT_DIRECTORY + "/kamel reset").waitFor();
        } catch (Exception e) {
            fail("Unable to invoke kamel binary", e);
        }
    }
}

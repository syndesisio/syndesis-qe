package io.syndesis.qe.util;

import io.syndesis.qe.MarketplaceTest;
import io.syndesis.qe.TestConfiguration;
import io.syndesis.qe.utils.OpenShiftUtils;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.fabric8.kubernetes.api.model.LocalObjectReference;
import io.fabric8.kubernetes.api.model.ServiceAccount;
import io.fabric8.kubernetes.api.model.ServiceAccountList;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HelperFunctions {
    public static String getPackageName(Path targetPath) throws IOException {
        try (Stream<Path> walk = Files.walk(targetPath)) {
            List<String> yamlFiles = walk.map(Path::toString)
                .filter(x -> x.endsWith(".package.yaml"))
                .collect(Collectors.toList());

            if (yamlFiles.size() > 0) {
                String []parts = yamlFiles.get(0).split("/");
                return parts[parts.length - 1].split("\\.")[0];
            }
        }

        return "";
    }

    public static String getOperatorVersion(Path targetPath) throws IOException {
        try (Stream<Path> walk = Files.walk(targetPath)) {
            List<String> directories = walk.filter(Files::isDirectory)
                .map(Path::toString)
                .sorted(Comparator.comparing(String::toString))
                .collect(Collectors.toList());

            if (directories.size() > 0) {
                String lastDir = directories.get(directories.size() - 1);
                String []parts = lastDir.split("/");
                return parts[parts.length - 1];
            }
        }

        return "";
    }

    public static String getOperatorName() {
        String[] parts = TestConfiguration.syndesisOperatorImage().split("/");
        String[] smallerParts = parts[parts.length - 1].split(":");
        return smallerParts[0];
    }

    public static String encodeFileToBase64Binary(String fileName) throws IOException {
        File file = new File(fileName);
        byte[] encoded = Base64.encodeBase64(FileUtils.readFileToByteArray(file));
        return new String(encoded, StandardCharsets.US_ASCII);
    }

    public static String readResource(String resourceName) throws IOException {
        StringBuffer sb = new StringBuffer("");
        URL url = MarketplaceTest.class.getClassLoader().getResource(resourceName);
        try (FileReader fileReader = new FileReader(url.getFile());
            BufferedReader bf = new BufferedReader(fileReader)) {

            String line;
            while ((line = bf.readLine()) != null) {
                sb.append(line).append("\n");
            }
        }

        return sb.toString();
    }

    public static String doPostRequest(String url, String body, String auth) throws IOException {
        HttpPost httpPost = new HttpPost(url);

        return executeRequest(httpPost, body, auth);
    }
    public static String doDeleteRequest(String url, String body, String auth) throws IOException {
        HttpDelete httpDelete = new HttpDelete(url);

        return executeRequest(httpDelete, body, auth);
    }

    private static String executeRequest(HttpUriRequest request, String body, String auth) throws IOException {
        HttpClient client = HttpClients.createDefault();

        // if we need to use body and if request supports payload then we can attach it
        if (body != null && request instanceof HttpEntityEnclosingRequest) {
            ((HttpEntityEnclosingRequest) request).setEntity(new StringEntity(body));
        }
        request.addHeader("Content-Type", "application/json; utf-8");
        request.addHeader("Accept", "application/json");
        if (auth != null) {
            request.addHeader("Authorization", auth);
        }

        HttpResponse httpResponse = client.execute(request);
        StringBuilder sb = new StringBuilder();
        if (httpResponse != null && httpResponse.getEntity() != null) {
            try (BufferedReader br = new BufferedReader(
                new InputStreamReader(httpResponse.getEntity().getContent(), StandardCharsets.UTF_8))) {
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    sb.append(responseLine.trim());
                }
            }
        }

        return sb.toString();
    }

    public static void linkPullSecret(String operatorName) {
        ServiceAccountList saList = OpenShiftUtils.getInstance().inNamespace(TestConfiguration.openShiftNamespace())
            .serviceAccounts().list();

        Optional<ServiceAccount> serviceAccount = saList.getItems().stream()
            .filter(sa -> sa.getMetadata().getName().equals(operatorName))
            .findFirst();

        if (serviceAccount.isPresent()) {
            ServiceAccount sa = serviceAccount.get();
            sa.getImagePullSecrets().add(new LocalObjectReference(TestConfiguration.syndesisPullSecretName()));
            OpenShiftUtils.getInstance().serviceAccounts().inNamespace(TestConfiguration.openShiftNamespace())
                .createOrReplace(sa);
        } else {
            log.error("Service account not found in resources");
        }

    }

    public static String copyManifestFilestFromImage(String image, String targetDirectory) throws IOException {
        if (checkSystemIsMac()) {
            targetDirectory = "/private" + targetDirectory;
        }

        ProcessBuilder builder = new ProcessBuilder(
            "docker", "run",
            "-v", targetDirectory + ":/opt/mount",
            "--rm",
            "--entrypoint", "cp",
            image,
            "-r", "/manifests", "/opt/mount/"
        );
        builder.redirectErrorStream(true);
        Process p = builder.start();

        BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));

        String line;
        StringBuilder sb = new StringBuilder();
        while ((line = r.readLine()) != null) {
            sb.append(line).append("\n");
        }

        return sb.toString();
    }

    private static boolean checkSystemIsMac() {
        String osName = System.getProperty("os.name").toLowerCase();
        return osName.contains("mac");
    }

    public static void replaceImageInManifest(Path file) throws IOException {
        List<String> fileContent = new ArrayList<>(Files.readAllLines(file, StandardCharsets.UTF_8));

        for (int i = 0; i < fileContent.size(); i++) {
            String newLine = fileContent.get(i).replaceAll("registry\\.stage\\.redhat\\.io/.+/" + getOperatorName() + ":.+$",
                TestConfiguration.syndesisOperatorImage());
            fileContent.set(i, newLine);
        }

        Files.write(file, fileContent, StandardCharsets.UTF_8);
    }
}

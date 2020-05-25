package io.syndesis.qe.utils;

import static org.assertj.core.api.Assertions.fail;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ODataUtils {

    public static String getOpenshiftRoute() {
        String host = OpenShiftUtils.getInstance().routes().withName("odata").get().getSpec().getHost();
        return "http://" + host + "/TripPin/odata.svc/";
    }

    public static String getOpenshiftService() {
        if (OpenShiftUtils.getInstance().getService("odata") != null) {
            String clusterIp = OpenShiftUtils.getInstance().getService("odata").getSpec().getClusterIP();
            return "http://" + clusterIp + ":8080/TripPin/odata.svc/";
        } else {
            return null;
        }
    }

    public static String readResourceFile(URL file) {
        if (file == null) {
            fail("File with name " + file + " doesn't exist in the resources");
        }
        log.info("Converting file " + file + " to String");
        String requestBody = null;
        try {
            requestBody = new String(Files.readAllBytes(Paths.get(file.getPath())));
        } catch (IOException e) {
            fail(file + " could not be loaded", e);
        }
        return requestBody;
    }

}

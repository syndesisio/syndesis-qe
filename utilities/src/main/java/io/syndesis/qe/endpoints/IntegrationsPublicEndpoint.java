package io.syndesis.qe.endpoints;

import static org.assertj.core.api.Assertions.fail;

import io.syndesis.common.model.integration.ContinuousDeliveryEnvironment;
import io.syndesis.server.endpoint.v1.handler.external.PublicApiHandler;

import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataOutput;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipFile;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@Lazy
public class IntegrationsPublicEndpoint extends PublicEndpoint {

    public IntegrationsPublicEndpoint() {
        super("/integrations");
    }

    /**
     * Import zip file with the integrations
     * endpoint -> POST ​/public​/integrations
     * original method ->
     * {@link io.syndesis.server.endpoint.v1.handler.external.PublicApiHandler#importResources(SecurityContext, PublicApiHandler.ImportFormDataInput)}
     *
     * @param tag - tag for imported integrations
     * @param name - name of the exported zip in target folder
     */
    public void importIntegration(String tag, String name) {
        MultipartFormDataOutput mdo = new MultipartFormDataOutput();
        try {
            mdo.addFormData("data", new FileInputStream(Paths.get("./target/" + name).toAbsolutePath().normalize().toFile()),
                MediaType.APPLICATION_OCTET_STREAM_TYPE);
            mdo.addFormData("environment", tag, MediaType.TEXT_PLAIN_TYPE);
        } catch (FileNotFoundException e) {
            fail("IO Exception during loading ZIP file.", e);
        }
        Invocation.Builder invocation = this.createInvocation(getWholeUrl(rootEndPoint));
        Response response = invocation.post(Entity.entity(mdo, MediaType.MULTIPART_FORM_DATA_TYPE));
        if (response.getStatus() != HttpURLConnection.HTTP_OK) {
            fail("Status of the response is " + response.getStatus());
        }
    }

    /**
     * Export all integrations which are tagged with particular tag.
     * endpoint -> GET ​/public​/integrations​/{env}​/export.zip
     * original method -> {@link io.syndesis.server.endpoint.v1.handler.external.PublicApiHandler#exportResources(String, boolean)}
     *
     * @param tag - tag for exporting
     * @param name - name of the exported zip in target folder
     */
    public void exportAllIntegrations(String tag, String name) {
        Invocation.Builder invocation = this.createInvocation(getWholeUrl(String.format(rootEndPoint + "/%s/export.zip?all=true", tag)));
        Response res = invocation.get();
        InputStream is = res.readEntity(InputStream.class);
        exportZip(is, name);
    }

    /**
     * Export all integrations which are tagged with particular tag.
     * endpoint -> GET ​/public​/integrations​/{env}​/export.zip
     * original method -> {@link io.syndesis.server.endpoint.v1.handler.external.PublicApiHandler#exportResources(String, boolean)}
     *
     * @param tag - tag for exporting
     * @param name - name of the exported zip in target folder. If name is null, only status code is returned and exported integrations are not saved
     * @param ignoreTimestamp - when it is set to true, all integrations according to tag are exported
     */
    public int exportIntegrationsAccordingToTag(String tag, String name, boolean ignoreTimestamp) {
        String url = String.format(rootEndPoint + "/%s/export.zip", tag);
        if (ignoreTimestamp) {
            url = url.concat("?ignoreTimestamp=true");
        }
        Invocation.Builder invocation = this.createInvocation(getWholeUrl(url));
        Response res = invocation.get();
        int statusCode = res.getStatus();
        if (statusCode == 200 && name != null) {
            InputStream is = res.readEntity(InputStream.class);
            exportZip(is, name);
        }
        return statusCode;
    }

    /**
     * Deploy particular integration, redeploy in case the integration is already published
     * endpoint -> POST /public​/integrations​/{id}​/deployments
     * original method -> {@link io.syndesis.server.endpoint.v1.handler.external.PublicApiHandler#publishIntegration(SecurityContext, String)}
     */
    public void deployIntegration(String integrationName) {
        Invocation.Builder invocation = this.createInvocation(getWholeUrl(String.format(rootEndPoint + "/%s/deployments", integrationName)));
        Response response = invocation.post(null);
        if (response.getStatus() != HttpURLConnection.HTTP_OK) {
            fail("Status of the response is " + response.getStatus());
        }
    }

    /**
     * Stop published integration
     * endpoint -> PUT /public​/integrations​/{id}​/deployments​/stop
     * original method -> {@link io.syndesis.server.endpoint.v1.handler.external.PublicApiHandler#stopIntegration(SecurityContext, String)}
     */
    public void stopIntegration(String integrationName) {
        Invocation.Builder invocation = this.createInvocation(getWholeUrl(String.format(rootEndPoint + "/%s/deployments/stop", integrationName)));
        Response response = invocation.put(null);
        if (response.getStatus() != HttpURLConnection.HTTP_NO_CONTENT) {
            fail("Status of the response is " + response.getStatus());
        }
    }

    /**
     * Get state of the particular integration
     * endpoint -> GET /public​/integrations​/{id}​/state
     * original method -> {@link io.syndesis.server.endpoint.v1.handler.external.PublicApiHandler#getIntegrationState(SecurityContext, String)}
     */
    public JsonNode getStateOfIntegration(String integrationName) {
        Invocation.Builder invocation = this.createInvocation(getWholeUrl(String.format(rootEndPoint + "/%s/state", integrationName)));
        Response response = invocation.get();
        if (response.getStatus() != HttpURLConnection.HTTP_OK) {
            fail("Status of the response is " + response.getStatus());
        }
        return response.readEntity(JsonNode.class); // cannot use original class
    }

    /**
     * Get all tags in the particular integration
     * endpoint -> GET /public​/integrations​/{id}​/tags
     * original method -> {@link io.syndesis.server.endpoint.v1.handler.external.PublicApiHandler#getReleaseTags(String)}
     */
    public Map<String, ContinuousDeliveryEnvironment> getAllTagsInIntegration(String integrationName) {
        Invocation.Builder invocation = this.createInvocation(getWholeUrl(String.format(rootEndPoint + "/%s/tags", integrationName)));
        Response response = invocation.get();
        if (response.getStatus() != HttpURLConnection.HTTP_OK) {
            fail("Status of the response is " + response.getStatus());
        }
        return response.readEntity(new GenericType<HashMap<String, ContinuousDeliveryEnvironment>>() {
        });
    }

    /**
     * Add tags to the particular integration
     * NOTE that uncheck existing tags on the integration
     * endpoint -> PUT ​/public​/integrations​/{id}​/tags
     * original method -> {@link io.syndesis.server.endpoint.v1.handler.external.PublicApiHandler#putTagsForRelease(String, List)}
     */
    public Map<String, ContinuousDeliveryEnvironment> updateTagsInIntegration(String integrationName, List<String> tags) {
        Invocation.Builder invocation = this.createInvocation(getWholeUrl(String.format(rootEndPoint + "/%s/tags", integrationName)));
        Response response = invocation.put(Entity.entity(tags, MediaType.APPLICATION_JSON));
        if (response.getStatus() != HttpURLConnection.HTTP_OK) {
            fail("Status of the response is " + response.getStatus());
        }
        return response.readEntity(new GenericType<HashMap<String, ContinuousDeliveryEnvironment>>() {
        });
    }

    /**
     * Add tags to the particular integration
     * NOTE that doesn't uncheck existing tags on the integration
     * endpoint -> PATCH ​/public​/integrations​/{id}​/tags
     * original method -> {@link io.syndesis.server.endpoint.v1.handler.external.PublicApiHandler#patchTagsForRelease(String, List)}
     */
    public Map<String, ContinuousDeliveryEnvironment> addTagsToIntegration(String integrationName, List<String> tags) {
        Invocation.Builder invocation = this.createInvocation(getWholeUrl(String.format(rootEndPoint + "/%s/tags", integrationName)));
        Response response = invocation.method("PATCH", Entity.entity(tags, MediaType.APPLICATION_JSON));
        if (response.getStatus() != HttpURLConnection.HTTP_OK) {
            fail("Status of the response is " + response.getStatus());
        }
        return response.readEntity(new GenericType<HashMap<String, ContinuousDeliveryEnvironment>>() {
        });
    }

    /**
     * Delete tag on particular integration
     * endpoint -> DELETE ​/public​/integrations​/{id}​/tags​/{env}
     * original method -> {@link io.syndesis.server.endpoint.v1.handler.external.PublicApiHandler#deleteReleaseTag(String, String)}
     */
    public void deleteTagInIntegration(String integrationName, String label) {
        Invocation.Builder invocation = this.createInvocation(getWholeUrl(String.format(rootEndPoint + "/%s/tags/%s", integrationName, label)));
        Response response = invocation.delete();
        if (response.getStatus() != HttpURLConnection.HTTP_NO_CONTENT) {
            fail("Status of the response is " + response.getStatus());
        }
    }

    /**
     * Function export InputStream as Zip file
     */
    private void exportZip(InputStream is, String name) {
        try {
            File targetFile = Paths.get("./target/" + name).toAbsolutePath().normalize().toFile();
            OutputStream outStream = new FileOutputStream(targetFile);
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                outStream.write(buffer, 0, bytesRead);
            }
            is.close();
            outStream.flush();
            outStream.close();
            checkThatZipFileIsValid(Paths.get("./target/" + name).toAbsolutePath().normalize().toFile());
        } catch (IOException e) {
            e.printStackTrace();
            fail("IO Exception during exporting ZIP file.", e);
        }
    }

    /**
     * Check that imported zip file is valid
     */
    private void checkThatZipFileIsValid(File file) {
        ZipFile zipfile = null;
        try {
            zipfile = new ZipFile(file);
            log.debug("Size of the zipfile is: " + zipfile.size());
        } catch (IOException e) {
            fail("The zip file is not valid");
        } finally {
            try {
                if (zipfile != null) {
                    zipfile.close();
                }
            } catch (IOException e) {
                fail("Error during closing zip file");
            }
        }
    }
}

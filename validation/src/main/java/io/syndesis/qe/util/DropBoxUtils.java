package io.syndesis.qe.util;

import io.syndesis.qe.account.Account;
import io.syndesis.qe.account.AccountsDirectory;

import org.springframework.stereotype.Component;

import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.GetMetadataErrorException;
import com.dropbox.core.v2.files.WriteMode;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class DropBoxUtils {
    private DbxClientV2 client;

    private DbxClientV2 getClient() throws DbxException {
        if (this.client == null) {
            System.setProperty("https.protocols", "TLSv1.2");
            Account account = AccountsDirectory.getInstance().get(Account.Name.DROPBOX);
            DbxRequestConfig config = new DbxRequestConfig(account.getProperty("clientIdentifier"));
            this.client = new DbxClientV2(config, account.getProperty("accessToken"));
            log.debug("DropBox client created, logged as: " + client.users().getCurrentAccount());
        } else {
            log.debug("DropBox client was already created, returning existing instance");
        }

        return this.client;
    }

    /**
     * Check if file with filePath exists - dropbox uses its own file structure
     *
     * @param filePath example: filePath = /folder/file.name
     * @return true if file exists
     */
    public boolean checkIfFileExists(String filePath) {
        //true by default - if no exception thrown
        boolean found = true;
        try {
            getClient().files().getMetadata(filePath);
        } catch (GetMetadataErrorException exception) {
            //this is expected outcome when file does not exist
            if (exception.errorValue.isPath() && exception.errorValue.getPathValue().isNotFound()) {
                found = false;
            }
        } catch (DbxException e) {
            //this will happen when dropbox api somehow fails
            log.error("Unexpected DropBox exception was thrown when checking if file {} exists.", filePath);
            e.printStackTrace();
            found = false;
        }
        return found;
    }

    /**
     * Example: deleteFile("/someFolder/file.name")
     *
     * @param filePath file path
     * @throws DbxException when something goes wrong with the api
     */
    public void deleteFile(String filePath) throws DbxException {
        getClient().files().deleteV2(filePath);
    }

    /**
     * Example: uploadFile("/someFolder/file.name", "whatever Text You Want Inside")
     *
     * @param filePath path in dropbox
     * @param text content
     * @throws IOException when something goes wrong with temp file
     * @throws DbxException when something goes wrong with the api
     */
    public void uploadFile(String filePath, String text) throws IOException, DbxException {
        File temp = File.createTempFile(filePath, "");
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(temp))) {
            temp.deleteOnExit();
            bw.write(text);
            bw.flush();
            try (InputStream is = new FileInputStream(temp)) {
                getClient().files().uploadBuilder(filePath)
                        .withMode(WriteMode.OVERWRITE).uploadAndFinish(is);
            }
        } catch (IOException ex) {
            log.error("Error with tmp file: " + ex);
        }
    }
}

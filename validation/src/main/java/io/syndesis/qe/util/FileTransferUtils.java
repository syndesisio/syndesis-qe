package io.syndesis.qe.util;

public interface FileTransferUtils {
    void deleteFile(String path);

    boolean isFileThere(String directory, String fileName);

    void uploadTestFile(String testFileName, String text, String remoteDirectory);

    String getFileContent(String directory, String fileName);
}

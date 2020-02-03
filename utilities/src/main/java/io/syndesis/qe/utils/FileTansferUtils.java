package io.syndesis.qe.utils;

public interface FileTansferUtils {
    public void deleteFile(String path);

    public boolean isFileThere(String directory, String fileName);

    public void uploadTestFile(String testFileName, String text, String remoteDirectory);

    public String getFileContent(String directory, String fileName);
}

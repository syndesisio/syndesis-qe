package io.syndesis.qe.tar;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.apache.commons.io.IOUtils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

public class Compress {

    private class Bundler extends SimpleFileVisitor<Path> {

        private TarArchiveOutputStream out;
        private Path toplevel;

        public Bundler(TarArchiveOutputStream output, Path dir) {
            out = output;
            toplevel = dir;
        }

        private String getEntryName(Path path) {
            return prefix.resolve(toplevel.relativize(path)).toString();
        }

        private InputStream getInputStream(Path path)
            throws FileNotFoundException {
            FileInputStream fin = new FileInputStream(path.toFile());
            return new BufferedInputStream(fin);
        }

        @Override
        public FileVisitResult preVisitDirectory(Path dir,
            BasicFileAttributes attrs) throws IOException {
            String entryName = getEntryName(dir);

            if (!"".equals(entryName)) {
                // We allow tars with no top-level, but we are not proud of it.
                TarArchiveEntry entry = new TarArchiveEntry(dir.toFile(), entryName);
                out.putArchiveEntry(entry);
                out.closeArchiveEntry();
            }

            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
            throws IOException {
            String entryName = getEntryName(file);

            TarArchiveEntry entry = new TarArchiveEntry(file.toFile(), entryName);
            out.putArchiveEntry(entry);
            IOUtils.copy(getInputStream(file), out);
            out.closeArchiveEntry();

            return FileVisitResult.CONTINUE;
        }

    }

    private TarArchiveOutputStream output;
    private Path prefix;

    public Compress(String outputFile, String prefix) throws IOException {
        FileOutputStream fileOut = new FileOutputStream(outputFile);
        BufferedOutputStream buffOut = new BufferedOutputStream(fileOut);
        GzipCompressorOutputStream gzOut = new GzipCompressorOutputStream(buffOut);
        output = new TarArchiveOutputStream(gzOut);

        this.prefix = Paths.get(prefix);
    }

    public void close() throws IOException {
        output.close();
    }

    public void writedir(Path dir) throws IOException {
        Bundler visitor = new Bundler(output, dir);
        Files.walkFileTree(dir, visitor);
    }
}

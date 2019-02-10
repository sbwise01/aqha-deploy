package com.bradandmarsha.aqha.deploy.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.log4j.Logger;

/**
 *
 * @author sbwise01
 */
public class TarUtil {
    private static final Logger LOGGER = Logger.getLogger(TarUtil.class);

    public static void compressDirectoryChildren(String archiveName, String directory) throws IOException {
        File dir = new File(directory);
        File[] children = dir.listFiles();
        try (TarArchiveOutputStream taos = getTarArchiveOutputStream(archiveName)){
            if (children != null){
                for (File child : children){
                    addToArchiveCompression(taos, child, ".");
                }
            }
        }
    }

    private static TarArchiveOutputStream getTarArchiveOutputStream(String name) throws IOException {
        TarArchiveOutputStream taos = new TarArchiveOutputStream(new GzipCompressorOutputStream(new FileOutputStream(name)));
        // TAR has an 8 gig file limit by default, this gets around that
        taos.setBigNumberMode(TarArchiveOutputStream.BIGNUMBER_STAR);
        // TAR originally didn't support long file names, so enable the support for it
        taos.setLongFileMode(TarArchiveOutputStream.LONGFILE_GNU);
        taos.setAddPaxHeadersForNonAsciiNames(true);
        
        return taos;
    }

    private static void addToArchiveCompression(TarArchiveOutputStream out, File file, String dir) throws IOException {
        String entry = dir + File.separator + file.getName();
        if (file.isFile()){
            out.putArchiveEntry(new TarArchiveEntry(file, entry));
            try (FileInputStream in = new FileInputStream(file)){
                IOUtils.copy(in, out);
            }
            out.closeArchiveEntry();
        } else if (file.isDirectory()) {
            File[] children = file.listFiles();
            if (children != null){
                for (File child : children){
                    addToArchiveCompression(out, child, entry);
                }
            }
        } else {
            LOGGER.info(file.getName() + " is not supported");
        }
    }
}

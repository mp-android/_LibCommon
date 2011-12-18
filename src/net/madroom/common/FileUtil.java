package net.madroom.common;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

public class FileUtil {

    private static final String SEPARATOR = File.separator;
    private static final String ENCODING = "UTF-8";
    private static final int BUFFER_SIZE = 1024 * 4;

    /***************************************************************************
     * file
     ***************************************************************************/
    public static void copyFile(File iFile, File oFile) throws IOException {
        FileInputStream fis = null;
        FileOutputStream fos = null;
        FileChannel input = null;
        FileChannel output = null;
        try {
            fis = new FileInputStream(iFile);
            fos = new FileOutputStream(oFile);
            input = fis.getChannel();
            output = fos.getChannel();
            input.transferTo(0, input.size(), output);
        } finally {
            closeFile(output);
            closeFile(fos);
            closeFile(input);
            closeFile(fis);
        }
    }

    public static void copyStream(InputStream input, OutputStream output) throws IOException {
        final byte[] buffer = new byte[BUFFER_SIZE];
        int read = 0;
        while (-1 != (read = input.read(buffer))) {
            output.write(buffer, 0, read);
        }
    }

    public static void writeString(File file, String data) throws IOException {
        final OutputStream output = new FileOutputStream(file);
        try {
            output.write(data.getBytes(ENCODING));
        } finally {
            closeFile(output);
        }
    }

    public static String readString(File file) throws IOException {
        final InputStream input = new FileInputStream(file);
        try {
            final StringBuilder result = new StringBuilder();
            final byte[] buffer = new byte[BUFFER_SIZE];
            int read = 0;
            while (-1 != (read = input.read(buffer))) {
                result.append(new String(buffer, 0, read, ENCODING));
            }
            return result.toString();
        } finally {
            closeFile(input);
        }
    }

    public static void closeFile(Closeable closeable) throws IOException {
        if (closeable != null) closeable.close();
    }

    /***************************************************************************
     * zip
     ***************************************************************************/
    public static void preZipDir(final File src, final File dest) throws IOException {
        if (!src.isDirectory()) {
            throw new IOException("Not a directory " + src);
        }
        if (src.list().length == 0) {
            return;
        }
        final ZipOutputStream output = new ZipOutputStream(new FileOutputStream(dest));
        try {
            zipDir(src, "", output);
        } finally {
            closeFile(output);
        }
    }

    public static void zipDir(final File src, final String dest, final ZipOutputStream output) throws IOException {
        for (final File file : src.listFiles()) {
            final String destFile = dest + SEPARATOR + file.getName();
            if (file.isDirectory()) {
                output.putNextEntry(new ZipEntry(destFile + SEPARATOR));
                zipDir(file, destFile, output);
                continue;
            }
            final InputStream input = new FileInputStream(file);
            try {
                output.putNextEntry(new ZipEntry(destFile));
                copyStream(input, output);
                output.flush();
                output.closeEntry();
            } finally {
                closeFile(input);
            }
        }
    }

    public static void unzip(final File src, final File dest) throws IOException {
        if (!dest.exists()) {
            if(!dest.mkdirs()) {
                throw new IOException("Make directory failed. " + dest);
            }
        }
        final ZipFile zipFile = new ZipFile(src);
        try {
            final Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                final ZipEntry entry = entries.nextElement();
                final File file = new File(dest, entry.getName());
                if (entry.isDirectory()) {
                    if(!file.mkdirs()) {
                        throw new IOException("Make directory failed. " + file);
                    }
                    continue;
                }
                copyStream(zipFile.getInputStream(entry), new FileOutputStream(file));
            }
        } finally {
            zipFile.close();
        }
    }


    /***************************************************************************
     * dir
     ***************************************************************************/
    public static void createDir(File dir) throws IOException {
        if (dir.exists()) {
            if (!dir.isDirectory()) {
                throw new IOException("File " + dir + " exists and is not a directory");
            }
        } else {
            if (!dir.mkdirs()) {
                if (!dir.isDirectory()) {
                    throw new IOException("Unable to create directory " + dir);
                }
            }
        }
    }

    public static boolean deleteDir(File dir) {
        if(dir==null) {
            return false;
        }
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }
        return dir.delete();
    }

    public static boolean delete(File file) {
        if (!file.isDirectory()) {
            return file.delete();
        }
        return false;
    }
}

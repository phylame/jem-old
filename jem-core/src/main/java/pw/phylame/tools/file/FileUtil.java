/*
 * Copyright 2015 Peng Wan
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package pw.phylame.tools.file;

import java.io.Reader;
import java.io.Writer;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.BufferedReader;
import java.io.RandomAccessFile;

import java.util.HashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Utility for file operations.
 */
public class FileUtil {

    /** Buffer area size */
    public static final int BUFFER_SIZE = 4096;

    /** Some known MIME types */
    private static HashMap<String, String> MIMEs = new HashMap<String, String>();

    /** Loads some known MIMEs from file. */
    private static void initBuiltinMime() {
        java.util.Properties prop = new java.util.Properties();
        InputStream in = FileUtil.class.getResourceAsStream("mime.properties");
        if (in == null) {       // not found file
            return;
        }
        try {
            prop.load(in);
        } catch (IOException e) {
            e.printStackTrace();
        }
        for (String fmt: prop.stringPropertyNames()) {
            MIMEs.put(fmt, prop.getProperty(fmt));
        }
    }

    static {
        initBuiltinMime();
    }

    /**
     * Returns the extension name of specified file name.
     * @param name name of file
     * @return string of extension. If not contain extension return {@code ""}.
     */
    public static String getExtension(String name) {
        int index = name.lastIndexOf(".");
        if (index >= 0) {
            return name.substring(index + 1);
        } else {
            return "";
        }
    }

    /**
     * Returns the MIME type of specified file name.
     * @param name path name of file
     * @return string of MIME.
     */
    public static String getMimeType(String name) {
        if (name == null || name.equals("")) {
            return "";
        }
        String mime = MIMEs.get(getExtension(name));
        if (mime != null) {
            return mime;
        } else {
            return new javax.activation.MimetypesFileTypeMap().getContentType(name);
        }
    }

    /**
     * Copies some number of bytes from <tt>InputStream</tt> to <tt>OutputStream</tt>.
     * @param in source stream
     * @param out destination stream
     * @param size the maximum number of bytes to be copied, if <tt>-1</tt> copies all
     * @return the total number of copied bytes
     * @throws java.io.IOException occurs IO errors
     */
    public static long copy(InputStream in, OutputStream out, long size) throws IOException {
        byte[] b = new byte[BUFFER_SIZE];
        long total = 0;
        int n;
        while ((n=in.read(b)) > 0) {
            total += n;
            if (size < 0) {                 // copy all bytes
                out.write(b, 0, n);
            } else if (total < size) {
                out.write(b, 0, n);
            } else {
                out.write(b, 0, (int)(n - (total - size)));
                total = size;
                break;
            }
        }
        return total;
    }

    /**
     * Copies some number of bytes from <tt>InputStream</tt> to <tt>RandomAccessFile</tt>.
     * @param in source stream
     * @param out destination file
     * @param size the maximum number of bytes to be copied, if <tt>-1</tt> copies all
     * @return the total number of copied bytes
     * @throws java.io.IOException occurs IO errors
     */
    public static long copy(InputStream in, RandomAccessFile out, long size) throws IOException {
        byte[] b = new byte[BUFFER_SIZE];
        long total = 0;
        int n;
        while ((n=in.read(b)) > 0) {
            total += n;
            if (size < 0) {                 // copy all bytes
                out.write(b, 0, n);
            } else if (total < size) {
                out.write(b, 0, n);
            } else {
                out.write(b, 0, (int)(n - (total - size)));
                total = size;
                break;
            }
        }
        return total;
    }

    /**
     * Copies some number of bytes from <tt>RandomAccessFile</tt> to <tt>OutputStream</tt>.
     * @param in source stream
     * @param out destination stream
     * @param size the maximum number of bytes to be copied, if <tt>-1</tt> copies all
     * @return the total number of copied bytes
     * @throws java.io.IOException occurs IO errors
     */
    public static long copy(RandomAccessFile in, OutputStream out, long size) throws IOException {
        byte[] b = new byte[BUFFER_SIZE];
        long total = 0;
        int n;
        while ((n=in.read(b)) > 0) {
            total += n;
            if (size < 0) {
                out.write(b, 0, n);
            } else if (total < size) {
                out.write(b, 0, n);
            } else {
                out.write(b, 0, (int)(n - total - size));
                total = size;
                break;
            }
        }
        return total;
    }

    /**
     * Copies some number of bytes from <tt>RandomAccessFile</tt> to <tt>RandomAccessFile</tt>.
     * @param in source stream
     * @param out destination file
     * @param size the maximum number of bytes to be copied, if <tt>-1</tt> copies all
     * @return the total number of copied bytes
     * @throws java.io.IOException occurs IO errors
     */
    public static long copy(RandomAccessFile in, RandomAccessFile out, long size) throws IOException {
        byte[] b = new byte[BUFFER_SIZE];
        long total = 0;
        int n;
        while ((n=in.read(b)) > 0) {
            total += n;
            if (size < 0) {
                out.write(b, 0, n);
            } else if (total < size) {
                out.write(b, 0, n);
            } else {
                out.write(b, 0, n - (int) (total - size));
                total = size;
                break;
            }
        }
        return total;
    }

    /**
     * Copies some number of characters from <tt>Reader</tt> to <tt>Writer</tt>.
     * @param in source reader
     * @param out destination writer
     * @param size the maximum number of characters to be copied, if <tt>-1</tt> copies all
     * @return the total number of copied characters
     * @throws java.io.IOException occurs IO errors
     */
    public static long copy(Reader in, Writer out, long size) throws IOException {
        char[] b = new char[BUFFER_SIZE];
        long total = 0;
        int n;
        while ((n=in.read(b)) > 0) {
            total += n;
            if (size < 0) {
                out.write(b, 0, n);
            } else if (total < size) {
                out.write(b, 0, n);
            } else {
                out.write(b, 0, (int)(n - (total - size)));
                total = size;
                break;
            }
        }
        return total;
    }

    /**
     * Copies some number of characters from <tt>Reader</tt> to <tt>Writer</tt>.
     * @param reader source reader
     * @param builder destination builder
     * @param size the maximum number of characters to be copied, if <tt>-1</tt> copies all
     * @return the total number of copied characters
     * @throws java.io.IOException occurs IO errors
     */
    public static long copy(Reader reader, StringBuilder builder, long size) throws IOException {
        char[] b = new char[BUFFER_SIZE];
        long total = 0;
        int n;
        while ((n=reader.read(b)) > 0) {
            total += n;
            if (size < 0) {
                builder.append(b, 0, n);
            } else if (total < size) {
                builder.append(b, 0, n);
            } else {
                builder.append(b, 0, (int)(n - (total - size)));
                total = size;
                break;
            }
        }
        return total;
    }

    /**
     * Writes content in <tt>FileObject</tt> to ZIP file with specified name.
     * @param out destination ZIP stream
     * @param name entry name in ZIP to store file
     * @param file <tt>FileObject</tt> to be written
     * @throws java.io.IOException occurs IO errors
     */
    public static void writeTo(ZipOutputStream out, String name, FileObject file) throws IOException {
        out.putNextEntry(new ZipEntry(name));
        file.copyTo(out);
    }

    /**
     * Reads all characters from <tt>Reader</tt>.
     * @param reader the <tt>Reader</tt> to be read
     * @return string in reader
     * @throws java.io.IOException occurs IO errors
     */
    public static String readText(Reader reader) throws IOException {
        StringBuilder builder = new StringBuilder();
        copy(reader, builder, -1);
        return builder.toString();
    }

    /**
     * Reads all characters from specified <tt>Reader</tt> and split to list by line separator.
     * @param reader the <tt>Reader</tt> to be read
     * @param skipEmptyLine <tt>true</tt> to skip empty lines otherwise keep all lines
     * @return split lines read from reader
     * @throws java.io.IOException occurs IO errors
     */
    public static String[] readLines(Reader reader, boolean skipEmptyLine) throws IOException {
        java.util.ArrayList<String> results = new java.util.ArrayList<String>();
        BufferedReader br;
        // make buffer reader
        if (reader instanceof BufferedReader) {
            br = (BufferedReader) reader;
        } else {
            br = new BufferedReader(reader);
        }
        String line;
        while ((line=br.readLine()) != null) {
            if (skipEmptyLine && line.isEmpty()) {
                continue;
            }
            results.add(line);
        }
        return results.toArray(new String[0]);
    }
}

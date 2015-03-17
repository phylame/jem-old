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

package pw.phylame.tools;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.ByteArrayOutputStream;

import java.util.Arrays;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

/**
 * Utility class for ZLib operations.
 */
public abstract class ZLibUtils {
    /** Buffer size */
    public static final int BUFFER_SIZE = 2048;

    public static byte[] compress(byte[] data) {
        return compress(data, Deflater.DEFAULT_COMPRESSION);
    }

    public static byte[] compress(byte[] data, int level) {
        return compress(data, 0, data.length, level);
    }

    public static byte[] compress(byte[] data, int offset, int length) {
        return compress(data, offset, length, Deflater.DEFAULT_COMPRESSION);
    }

    public static byte[] compress(byte[] data, int offset, int length, int level) {
        byte[] output = new byte[0];
        Deflater compresser = new Deflater(level);
        compresser.reset();
        compresser.setInput(data, offset, length);
        compresser.finish();
        ByteArrayOutputStream bos = new ByteArrayOutputStream(length);
        try {
            byte[] buf = new byte[BUFFER_SIZE];
            while (!compresser.finished()) {
                int i = compresser.deflate(buf);
                bos.write(buf, 0, i);
            }
            output = bos.toByteArray();
        } catch (Exception e) {
            output = Arrays.copyOfRange(data, offset, offset+length);
            e.printStackTrace();
        } finally {
            try {
                bos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        compresser.end();
        return output;
    }

    public static void compress(byte[] data, OutputStream os) {
        compress(data, 0, data.length, os);
    }

    public static void compress(byte[] data, int offset, int length, OutputStream os) {
        DeflaterOutputStream dos = new DeflaterOutputStream(os);
        try {
            dos.write(data, offset, length);
            dos.finish();
            dos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static byte[] decompress(byte[] data) {
        return decompress(data, 0, data.length);
    }

    public static byte[] decompress(byte[] data, int offset, int length) {
        byte[] output = new byte[0];
        Inflater decompresser = new Inflater();
        decompresser.reset();
        decompresser.setInput(data, offset, length);
        ByteArrayOutputStream o = new ByteArrayOutputStream(length);
        try {
            byte[] buf = new byte[BUFFER_SIZE];
            while (!decompresser.finished()) {
                int i = decompresser.inflate(buf);
                o.write(buf, 0, i);
            }
            output = o.toByteArray();
        } catch (Exception e) {
            output = Arrays.copyOfRange(data, offset, offset+length);
            e.printStackTrace();
        } finally {
            try {
                o.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        decompresser.end();
        return output;
    }

    public static byte[] decompress(InputStream is) {
        InflaterInputStream iis = new InflaterInputStream(is);
        ByteArrayOutputStream o = new ByteArrayOutputStream(BUFFER_SIZE);
        try {
            int i = BUFFER_SIZE;
            byte[] buf = new byte[i];

            while ((i = iis.read(buf, 0, i)) > 0) {
                o.write(buf, 0, i);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return o.toByteArray();
    }
}

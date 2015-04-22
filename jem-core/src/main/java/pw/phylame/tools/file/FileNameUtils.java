/*
 * Copyright 2014-2015 Peng Wan <phylame@163.com>
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

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

/**
 * Utility class for file name.
 */
public class FileNameUtils {
    /** Some known MIME types */
    private static HashMap<String, String> MIMEs = new HashMap<String, String>();
    static {
        initBuiltinMime();
    }

    /** Loads some known MIMEs from file. */
    static void initBuiltinMime() {
        java.util.Properties prop = new java.util.Properties();
        InputStream in = FileUtils.class.getResourceAsStream("mime.properties");
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

    /**
     * Returns the MIME type of specified file name.
     * @param name path name of file
     * @return string of MIME.
     */
    public static String getMimeType(String name) {
        if (name == null || name.equals("")) {
            return "";
        }
        String mime = MIMEs.get(extensionName(name));
        if (mime != null) {
            return mime;
        } else {
            return new javax.activation.MimetypesFileTypeMap().getContentType(name);
        }
    }


    /**
     * Returns the extension name of specified file name.
     * @param name name of file
     * @return string of extension. If not contain extension return {@code ""}.
     */
    public static String extensionName(String name) {
        int index = name.lastIndexOf(".");
        if (index >= 0) {
            return name.substring(index + 1);
        } else {
            return "";
        }
    }

    /**
     * Returns base name in specified file name.
     * @param name file name
     * @return the base name
     */
    public static String baseName(String name) {
        int start = 0, end = name.length();

        for (int ix = name.length()-1; ix >= 0; --ix) {
            char c = name.charAt(ix);
            if (c == '.') {
                end = ix;
            } else if (c == '/' || c == '\\') {
                start = ix+1;
                break;      // stop
            }
        }

        if (end >= start) {
            return name.substring(start, end);
        }
        return "";
    }
}

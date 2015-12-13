/*
 * Copyright 2014-2015 Peng Wan <phylame@163.com>
 *
 * This file is part of Jem.
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

package pw.phylame.jem.util;

import java.io.*;
import java.net.URL;
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Iterator for reading lines in resources.
 */
public class LineIterator implements Iterator<String> {
    private static final Log LOG = LogFactory.getLog(LineIterator.class);

    public String commentLabel = "#";
    public boolean trimSpace = true;
    public boolean skipEmpty = true;

    private BufferedReader reader = null;
    private String currentLine = null;

    public LineIterator(String path, String encoding) {
        try {
            prepareReader(new FileInputStream(path), encoding);
        } catch (FileNotFoundException e) {
            LOG.debug("cannot open reader for " + getClass().getSimpleName(), e);
        }
    }

    public LineIterator(URL url, String encoding) {
        try {
            prepareReader(url.openStream(), encoding);
        } catch (IOException e) {
            LOG.debug("cannot open reader for " + getClass().getSimpleName(), e);
        }
    }

    public LineIterator(InputStream input, String encoding) {
        prepareReader(input, encoding);
    }

    private void prepareReader(InputStream input, String encoding) {
        Reader reader = null;
        try {
            if (encoding == null) {
                reader = new InputStreamReader(input);
            } else {
                reader = new InputStreamReader(input, encoding);
            }
        } catch (UnsupportedEncodingException e) {
            LOG.debug("cannot open reader for " + getClass().getSimpleName(), e);
        }
        if (reader != null) {
            this.reader = new BufferedReader(reader);
            currentLine = nextLine();
        }
    }

    private String nextLine() {
        try {
            String line;
            while (true) {
                line = reader.readLine();
                if (line == null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        LOG.debug("cannot close reader of " + getClass().getSimpleName(), e);
                    }
                    break;
                }
                if (trimSpace) {
                    line = line.trim();
                }
                if (skipEmpty && line.isEmpty()) {
                    continue;
                }
                if (commentLabel != null && line.startsWith(commentLabel)) {
                    continue;
                }
                break;
            }
            return line;
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    public boolean hasNext() {
        return currentLine != null;
    }

    @Override
    public String next() {
        if (currentLine == null) {
            throw new NoSuchElementException();
        }
        String line = currentLine;
        currentLine = nextLine();
        return line;
    }
}

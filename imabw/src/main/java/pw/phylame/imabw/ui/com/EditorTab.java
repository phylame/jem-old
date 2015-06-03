/*
 * Copyright 2014-2015 Peng Wan <phylame@163.com>
 *
 * This file is part of Imabw.
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

package pw.phylame.imabw.ui.com;

import java.io.File;
import java.io.IOException;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;

import pw.phylame.jem.core.Part;
import pw.phylame.jem.core.Cleanable;
import pw.phylame.tools.file.FileFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class EditorTab {
    private static Log LOG = LogFactory.getLog(EditorTab.class);

    public static final String CACHE_ENCODING = "UTF-16BE";

    private ITextEdit textEdit;
    private Part part;
    private String encoding;
    private boolean modified = false;
    private File file = null;

    public EditorTab(ITextEdit textEdit, Part part, String encoding) {
        this.textEdit = textEdit;
        this.part = part;
        this.encoding = encoding;
    }

    public ITextEdit getTextEdit() {
        return textEdit;
    }

    public Part getPart() {
        return part;
    }

    public String getEncoding() {
        return encoding;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    public boolean isModified() {
        return modified;
    }

    public void setModified(boolean modified) {
        this.modified = modified;
    }

    // cache text content in textEdit to temporary file
    public void cacheContent() {
        if (! isModified()) {
            return;
        }
        BufferedWriter writer = null;
        try {
            if (file == null) {
                file = File.createTempFile("imabw_", ".itf");
                part.registerCleanup(new Cleanable() {
                    @Override
                    public void clean(Part part) {
                        if (! file.delete()) {
                            LOG.debug("cannot delete cached file: "+file);
                        }
                    }
                });
            }
            writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file),
                    CACHE_ENCODING));
            textEdit.write(writer);
            part.getSource().setFile(FileFactory.fromFile(file, null), CACHE_ENCODING);
            modified = false;
        } catch (IOException e) {
            LOG.debug("cannot cache part content: "+part.getTitle(), e);
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    LOG.debug("cannot close cached file: "+file);
                }
            }
        }
    }
}

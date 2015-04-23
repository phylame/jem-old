/*
 * Copyright 2015 Peng Wan <phylame@163.com>
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import pw.phylame.ixin.ITextEdit;
import pw.phylame.jem.core.Part;
import pw.phylame.tools.file.FileFactory;

import java.io.*;

public class EditorTab {
    private static Log LOG = LogFactory.getLog(EditorTab.class);

    public static final String CACHE_ENCODING = "UTF-16BE";

    private ITextEdit textEdit;
    private Part part;
    private boolean modified = false;
    private File file = null;

    public EditorTab(ITextEdit textEdit, Part part) {
        this.textEdit = textEdit;
        this.part = part;
    }

    public ITextEdit getTextEdit() {
        return textEdit;
    }

    public void setTextEdit(ITextEdit textEdit) {
        this.textEdit = textEdit;
    }

    public Part getPart() {
        return part;
    }

    public void setPart(Part part) {
        this.part = part;
    }

    public boolean isModified() {
        return modified;
    }

    public void setModified(boolean modified) {
        this.modified = modified;
    }

    // cache text content in textEdit to file
    public void cache() throws IOException {
        if (! isModified()) {
            return;
        }
        if (file == null) {
            file = File.createTempFile("IMABW_", ".tmp");
            getPart().registerCleanup(new Part.Cleanable() {
                @Override
                public void clean(Part part) {
                    if (! file.delete()) {
                        LOG.debug("cannot delete cached file: "+file);
                    }
                }
            });
        }
        Writer writer = null;
        try {
            writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), CACHE_ENCODING));
            textEdit.write(writer);
            part.getSource().setFile(FileFactory.getFile(file, null), CACHE_ENCODING);
            modified = false;
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
    }
}

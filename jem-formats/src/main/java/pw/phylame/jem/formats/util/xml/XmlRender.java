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

package pw.phylame.jem.formats.util.xml;

import org.xmlpull.v1.XmlSerializer;
import pw.phylame.jem.formats.util.MakerException;
import pw.phylame.jem.formats.util.text.TextUtils;

import java.io.Writer;
import java.io.IOException;
import java.io.OutputStream;
import java.util.LinkedList;

/**
 * Renders XML document.
 */
public class XmlRender {
    private XmlSerializer xmlSerializer;
    private XmlConfig config;

    private boolean doIndent;
    private int indentCount;

    private LinkedList<TagEntry> tagStack = new LinkedList<TagEntry>();

    public XmlRender(XmlConfig config) throws MakerException {
        this.xmlSerializer = XmlUtils.newSerializer();
        this.config = config;
        doIndent = TextUtils.isValid(config.indentString);
    }

    public void setOutput(Writer writer) throws IOException {
        xmlSerializer.setOutput(writer);
    }

    public void setOutput(OutputStream outputStream) throws IOException {
        xmlSerializer.setOutput(outputStream, config.encoding);
    }

    public void flush() throws IOException {
        xmlSerializer.flush();
    }

    public void startXml() throws IOException {
        xmlSerializer.startDocument(config.encoding, config.standalone);
        reset();
    }

    public void endXml() throws IOException {
        xmlSerializer.endDocument();
        flush();
    }

    public void reset() {
        tagStack.clear();
        indentCount = 0;
    }

    public void docdecl(String root, String id, String url) throws IOException {
        docdecl(root + " PUBLIC \"" + id + "\" \"" + url + "\"");
    }

    public void docdecl(String text) throws IOException {
        xmlSerializer.text(config.lineSeparator);
        xmlSerializer.docdecl(" " + text);
    }

    private void indent(int count) throws IOException {
        if (count <= 0) {
            return;
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < count; i++) {
            sb.append(config.indentString);
        }
        xmlSerializer.text(sb.toString());
    }

    private void newNode() throws IOException {
        xmlSerializer.text(config.lineSeparator);   // node in new line
        if (doIndent) {
            indent(indentCount);
        }
        if (!tagStack.isEmpty()) {
            tagStack.getFirst().hasSubTag = true;
        }
    }

    public XmlRender comment(String text) throws IOException {
        newNode();
        xmlSerializer.comment(text);
        return this;
    }

    public XmlRender startTag(String name) throws IOException {
        return startTag(null, name);
    }

    public XmlRender startTag(String namespace, String name) throws IOException {
        newNode();
        ++indentCount;
        xmlSerializer.startTag(namespace, name);
        tagStack.push(new TagEntry(namespace, name));
        return this;
    }

    public XmlRender attribute(String name, String value) throws IOException {
        xmlSerializer.attribute(null, name, value);
        return this;
    }

    public XmlRender attribute(String namespace, String name, String value)
            throws IOException {
        xmlSerializer.attribute(namespace, name, value);
        return this;
    }

    public XmlRender text(String text) throws IOException {
        xmlSerializer.text(text);
        return this;
    }

    public XmlRender endTag() throws IOException {
        if (tagStack.isEmpty()) {
            throw new AssertionError("startTag should be called firstly");
        }
        TagEntry tagEntry = tagStack.pop();
        if (tagEntry.hasSubTag) {
            xmlSerializer.text(config.lineSeparator);
            if (doIndent) {
                indent(indentCount - 1);
            }
        }
        --indentCount;
        xmlSerializer.endTag(tagEntry.namespace, tagEntry.name);
        return this;
    }

    private class TagEntry {
        String namespace;
        String name;

        // for endTag, if hasSubTag add line separator and indent
        boolean hasSubTag = false;

        TagEntry(String namespace, String name) {
            this.namespace = namespace;
            this.name = name;
        }
    }
}

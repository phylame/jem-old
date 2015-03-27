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

package pw.phylame.jem.formats.pmab;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import pw.phylame.tools.TextObject;
import pw.phylame.tools.file.FileObject;
import pw.phylame.tools.file.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Defines constants and common methods.
 */
public class Pmab {
    private static Log LOG = LogFactory.getLog(Pmab.class);

    ///// MIME type for PMAB /////
    static final String MIME_FILE = "mimetype";
    static final String MT_PMAB = "application/pmab+zip";

    ///// PBM(PMAB Book Metadata) /////
    static final String PBM_FILE = "book.xml";
    public static final String PBM_XML_NS = "http://phylame.pw/format/pmab/pbm";

    ///// PBC(PMAB Book Content) /////
    static final String PBC_FILE = "content.xml";
    public static final String PBC_XML_NS = "http://phylame.pw/format/pmab/pbc";

    public static boolean isPmab(ZipFile zipFile) {
        InputStream stream = null;
        try {
            stream = zipFile.getInputStream(zipFile.getEntry(MIME_FILE));
            String text = FileUtils.readText(new InputStreamReader(stream)).trim();
            return MT_PMAB.equals(text);
        } catch (IOException e) {
            LOG.debug("cannot load "+MIME_FILE, e);
            return false;
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e) {
                    LOG.debug("cannot close "+MIME_FILE, e);
                }
            }
        }
    }

    /**
     * Makes XML {@code org.dom4j.io.OutputFormat}.
     * @param encoding encoding for XML.
     * @param indent indent line
     * @param lineSeparator line separator
     * @return {@code org.dom4j.io.OutputFormat}
     */
    private static org.dom4j.io.OutputFormat getXMLFormat(String encoding, String indent, String lineSeparator) {
        org.dom4j.io.OutputFormat format = org.dom4j.io.OutputFormat.createPrettyPrint();
        format.setEncoding(encoding);
        format.setIndent(indent);
        format.setLineSeparator(lineSeparator);
        format.setTrimText(false);
        return format;
    }

    public static void writeXML(org.dom4j.Document doc, java.io.OutputStream out,
                                String encoding, String indent, String lineSeparator) throws IOException {
        org.dom4j.io.XMLWriter xmlWriter = new org.dom4j.io.XMLWriter(out,
                getXMLFormat(encoding, indent, lineSeparator));
        xmlWriter.write(doc);
    }

    public static void writeFile(FileObject fb, ZipOutputStream zipout, String href) throws IOException {
        zipout.putNextEntry(new ZipEntry(href));
        fb.copyTo(zipout);
        zipout.closeEntry();
    }

    /**
     * Writes text content in TextObject to PMAB archive.
     * @param tb the TextObject
     * @param zipout PMAB archive stream
     * @param href name of entry to store text content
     * @param encoding encoding to encode text
     */
    public static void writeText(TextObject tb, ZipOutputStream zipout, String href, String encoding)
            throws IOException {
        zipout.putNextEntry(new ZipEntry(href));
        java.io.Writer writer = new java.io.OutputStreamWriter(zipout, encoding);
        tb.writeTo(writer);
        writer.flush();
        zipout.closeEntry();
    }
}

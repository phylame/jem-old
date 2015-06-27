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

package pw.phylame.jem.formats.util;

import org.dom4j.Document;
import org.dom4j.io.XMLWriter;
import org.dom4j.io.OutputFormat;

import java.io.IOException;
import java.io.OutputStream;

/**
 * XML utilities.
 */
public class XmlUtils {
    private static OutputFormat getXMLFormat(String encoding, String indent,
                                             String lineSeparator) {
        OutputFormat format = OutputFormat.createPrettyPrint();
        format.setEncoding(encoding);
        format.setIndent(indent);
        format.setLineSeparator(lineSeparator);
        format.setTrimText(false);
        return format;
    }

    public static void writeXML(Document doc, OutputStream out, String encoding,
                                String indent, String lineSeparator) throws IOException {
        XMLWriter xmlWriter = new XMLWriter(out,
                getXMLFormat(encoding, indent, lineSeparator));
        xmlWriter.write(doc);
    }
}

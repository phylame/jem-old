/*
 * Copyright 2015 Peng Wan <phylame@163.com>
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

import org.dom4j.Element;
import org.dom4j.Document;
import org.dom4j.io.SAXReader;
import org.dom4j.DocumentException;

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import pw.phylame.jem.core.Book;
import pw.phylame.jem.core.Parser;
import pw.phylame.jem.core.Part;
import pw.phylame.jem.formats.pmab.reader.ReaderV1;
import pw.phylame.jem.formats.pmab.reader.ReaderV2;
import pw.phylame.jem.formats.pmab.reader.ReaderV3;
import pw.phylame.jem.util.JemException;

import java.io.*;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * <tt>Parser</tt> implement for PMAB book.
 */
public class PmabParser implements Parser {
    private static Log LOG = LogFactory.getLog(PmabParser.class);

    /**
     * Returns the format name(normally the extension name).
     */
    @Override
    public String getName() {
        return "pmab";
    }

    /**
     * Parses book file and stores content as <tt>Book</tt>.
     *
     * @param file the book file
     * @param kw   arguments to the parser
     * @return <tt>Book</tt> represents the book file
     * @throws java.io.IOException              occurs IO errors
     * @throws pw.phylame.jem.util.JemException occurs errors when parsing book file
     */
    @Override
    public Book parse(File file, Map<String, Object> kw) throws IOException, JemException {
        ZipFile zipFile = new ZipFile(file);
        try {
            return parse(zipFile);
        } catch (IOException ex) {
            zipFile.close();
            throw ex;
        } catch (JemException ex) {
            zipFile.close();
            throw ex;
        }
    }

    public Book parse(final ZipFile zipFile) throws IOException, JemException {
        if (! Pmab.isPmab(zipFile)) {
            throw new JemException("Invalid PMAB archive");
        }
        Book book = new Book();
        readPBM(zipFile, book);
        readPBC(zipFile, book);
        book.registerCleanup(new Part.Cleanable() {
            @Override
            public void clean(Part part) {
                try {
                    zipFile.close();
                } catch (IOException e) {
                    LOG.debug("cannot close PMAB source", e);
                }
            }
        });
        return book;
    }

    private void readPBM(ZipFile zipFile, Book book) throws IOException, JemException {
        ZipEntry zipEntry = zipFile.getEntry(Pmab.PBM_FILE);
        if (zipEntry == null) {
            throw new IOException("Not found "+Pmab.PBM_FILE+" in PMAB "+zipFile.getName());
        }
        InputStream input = new BufferedInputStream(zipFile.getInputStream(zipEntry));
        SAXReader reader = new SAXReader();
        reader.setEntityResolver(new EntityResolver() {
            @Override
            public InputSource resolveEntity(String publicId, String systemId)
                    throws SAXException, IOException {
                return new InputSource(new ByteArrayInputStream("".getBytes()));
            }
        });
        Document doc;
        try {
            doc = reader.read(input);
        } catch (DocumentException e) {
            e.printStackTrace();
            throw new JemException("Invalid PBM document in "+zipFile.getName(), e);
        }
        Element root = doc.getRootElement();
        String version;
        String tag = root.getName();
        if ("pbm".equals(tag)) {
            // check DTD if present
            if (doc.getDocType() != null && ! doc.getDocType().getName().equals("pbm")) {
                throw new JemException("Invalid PBC document: DTD is not pbm");
            }
            version = root.attributeValue("version");
        } else if ("package".equals(tag)) {
            // check DTD if present
            if (doc.getDocType() != null && ! doc.getDocType().getName().equals("package")) {
                throw new JemException("Invalid PBC document: DTD is not package");
            }
            version = "1.0";
        } else {
            input.close();
            throw new JemException("Invalid PBM document: root is not pbm or package");
        }
        if ("2.0".equals(version)) {
            ReaderV2.readPBM(root, book, zipFile);
        } else if ("3.0".equals(version)) {
            ReaderV3.readPBM(root, book, zipFile);
        } else  if ("1.0".equals(version)) {
            ReaderV1.readPBM(root, book, zipFile);
        } else {
            input.close();
            throw new JemException("Invalid PBM version: "+version);
        }
        input.close();
    }

    private void readPBC(ZipFile zipFile, Book book) throws IOException, JemException {
        ZipEntry zipEntry = zipFile.getEntry(Pmab.PBC_FILE);
        if (zipEntry == null) {
            throw new IOException("Not found "+Pmab.PBC_FILE+" in PMAB "+zipFile.getName());
        }
        InputStream input = new BufferedInputStream(zipFile.getInputStream(zipEntry));
        SAXReader reader = new SAXReader();
        reader.setEntityResolver(new EntityResolver() {
            @Override
            public InputSource resolveEntity(String publicId, String systemId)
                    throws SAXException, IOException {
                return new InputSource(new ByteArrayInputStream("".getBytes()));
            }
        });
        Document doc;
        try {
            doc = reader.read(input);
        } catch (DocumentException e) {
            e.printStackTrace();
            throw new JemException("Invalid PBC document in "+zipFile.getName(), e);
        }
        Element root = doc.getRootElement();
        String version;
        String tag = root.getName();
        if ("pbc".equals(tag)) {
            // check DTD if present
            if (doc.getDocType() != null && ! doc.getDocType().getName().equals("pbc")) {
                throw new JemException("Invalid PBC document: DTD is not pbc");
            }
            version = root.attributeValue("version");
        } else if ("container".equals(tag)) {
            // check DTD if present
            if (doc.getDocType() != null && ! doc.getDocType().getName().equals("container")) {
                throw new JemException("Invalid PBC document: DTD is not container");
            }
            version = "1.0";
        } else {
            input.close();
            throw new JemException("Invalid PBC document: root is not pbc or container");
        }
        if ("2.0".equals(version)) {
            ReaderV2.readPBC(root, book, zipFile);
        } else if ("3.0".equals(version)) {
            ReaderV3.readPBC(root, book, zipFile);
        } else  if ("1.0".equals(version)) {
            ReaderV1.readPBC(root, book, zipFile);
        } else {
            input.close();
            throw new JemException("Invalid PBC version: "+version);
        }
        input.close();
    }
}

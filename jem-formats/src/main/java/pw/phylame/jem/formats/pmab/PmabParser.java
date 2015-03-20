/*
 * Copyright 2015 Peng Wan
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

import pw.phylame.jem.core.Book;
import pw.phylame.jem.core.Parser;
import pw.phylame.jem.core.Part;
import pw.phylame.jem.util.JemException;

import java.io.BufferedInputStream;
import java.util.Map;
import java.io.File;
import java.io.InputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import pw.phylame.jem.formats.pmab.reader.*;

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
            throw new JemException("invalid PMAB archive");
        }
        Book book = new Book();
        try {
            readPBM(zipFile, book);
        } catch (XMLStreamException e) {
            throw new JemException("Invalid PBM document", e);
        }
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

    private void readPBM(ZipFile zipFile, Book book) throws IOException, JemException, XMLStreamException {
        ZipEntry zipEntry = zipFile.getEntry(Pmab.PBM_FILE);
        if (zipEntry == null) {
            throw new IOException("Not found "+Pmab.PBM_FILE+" in PMAB "+zipFile.getName());
        }
        InputStream input = new BufferedInputStream(zipFile.getInputStream(zipEntry));
        XMLInputFactory inputFactory = XMLInputFactory.newFactory();
        XMLStreamReader streamReader = inputFactory.createXMLStreamReader(input);
        try {
            int event = streamReader.getEventType();
            while (true) {
                switch (event) {
                    case XMLStreamConstants.START_ELEMENT:
                        System.out.println(streamReader.getLocalName());
                        break;
                }
                if (! streamReader.hasNext()) {
                    break;
                }
                event = streamReader.next();
            }
        } finally {
            streamReader.close();
        }
        input.close();
//        try {
//            while (streamReader.hasNext()) {
//                switch (streamReader.next()) {
//                    case XMLStreamConstants.DTD:
//                        // TODO add DTD check
//                        break;
//                    case XMLStreamConstants.START_ELEMENT:     // check root element
//                        String name = streamReader.getLocalName();
//                        String version;
//                        if ("pbm".equals(name)) {
//                            version = streamReader.getAttributeValue(null, "version");
//                        } else if ("package".equals(name)) {    // PMAB 1.x
//                            version = "1.0";
//                        } else {
//                            streamReader.close();
//                            input.close();
//                            throw new JemException("Invalid PBM document in "+zipFile.getName());
//                        }
//                        if ("2.0".equals(version)) {
//                            ReaderV2.readPBM(streamReader, book, zipFile);
//                        } else if ("3.0".equals(version)) {
//                            ReaderV3.readPBM(streamReader, book, zipFile);
//                        } else if ("1.0".equals(version)) {
//                            ReaderV1.readPBM(streamReader, book, zipFile);
//                        } else {
//                            streamReader.close();
//                            input.close();
//                            throw new JemException("Unsupported PMAB version: "+version);
//                        }
//                        streamReader.close();
//                        input.close();
//                        return;
//                }
//            }
//        } catch (XMLStreamException e) {
//            throw new JemException("Invalid PBM document in "+zipFile.getName(), e);
//        }
    }

    private void readPBC(ZipFile zipFile, Book book) {

    }
}

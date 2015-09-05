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
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Defines constants and common methods.
 */
public final class PMAB {
    private static Log LOG = LogFactory.getLog(PMAB.class);

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
        ZipEntry entry = zipFile.getEntry(MIME_FILE);
        if (entry == null) {
            return false;
        }
        InputStream stream = null;
        try {
            stream = zipFile.getInputStream(entry);
            String text = IOUtils.toString(stream).trim();
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
}

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
import java.util.zip.ZipFile;
import pw.phylame.tools.file.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Created by Peng Wan on 2015-3-19.
 */
public class Pmab {
    private static Log LOG = LogFactory.getLog(Pmab.class);

    ///// MIME type for PMAB /////
    static final String MIME_FILE = "mimetype";
    static final String MT_PMAB = "application/pmab+zip";


    public static boolean isPmab(ZipFile zipFile) {
        InputStream in;
        try {
            in = zipFile.getInputStream(zipFile.getEntry(MIME_FILE));
        } catch (IOException e) {
            LOG.debug("cannot load "+MIME_FILE, e);
            return false;
        }
        try {
            String text = FileUtils.readText(new InputStreamReader(in)).trim();
            return MT_PMAB.equals(text);
        } catch (IOException e) {
            LOG.debug("cannot load " + MIME_FILE, e);
            return false;
        }
    }
}

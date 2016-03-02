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

import java.util.Map;

import pw.phylame.jem.formats.common.ZipMakeConfig;
import pw.phylame.jem.formats.util.config.ConfigKey;
import pw.phylame.jem.formats.util.xml.XmlConfig;

/**
 * Config for making PMAB book.
 */
public class PmabMakeConfig extends ZipMakeConfig {
    public static final String CONFIG_SELF = "pmab.make.config";

    public static final String VERSION = "pmab.make.version";
    public static final String TEXT_DIR = "pmab.make.textDir";
    public static final String IMAGE_DIR = "pmab.make.imageDir";
    public static final String EXTRA_DIR = "pmab.make.extraDir";
    public static final String XML_CONFIG = "pmab.make.xmlConfig";
    public static final String TEXT_ENCODING = "pmab.make.encoding";
    public static final String DATE_FORMAT = "pmab.make.dateFormat";
    public static final String META_INFO = "pmab.make.metaInfo";

    /**
     * Output PMAB version
     */
    @ConfigKey(VERSION)
    public String version = "3.0";

    /**
     * Directory in PMAB for storing text.
     */
    @ConfigKey(TEXT_DIR)
    public String textDir = "text";

    /**
     * Directory in PMAB for storing images.
     */
    @ConfigKey(IMAGE_DIR)
    public String imageDir = "images";

    /**
     * Directory in PMAB for storing extra file(s).
     */
    @ConfigKey(EXTRA_DIR)
    public String extraDir = "extras";

    /**
     * XML render config.
     */
    @ConfigKey(XML_CONFIG)
    public XmlConfig xmlConfig = new XmlConfig();

    /**
     * Encoding for converting all text in PMAB.
     */
    @ConfigKey(TEXT_ENCODING)
    public String textEncoding = PMAB.defaultEncoding;

    /**
     * Format for storing <tt>Date</tt> value.
     */
    @ConfigKey(DATE_FORMAT)
    public String dateFormat = "yyyy-M-d";

    /**
     * Addition information to PMAB archive.
     * <p><strong>NOTE:</strong> The key and value stored as String.
     */
    @ConfigKey(META_INFO)
    public Map<Object, Object> metaInfo = null;

    @Override
    public void adjust() {
        xmlConfig.standalone = true;
    }
}

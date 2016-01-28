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

import pw.phylame.jem.formats.common.ZipMakerConfig;
import pw.phylame.jem.formats.util.xml.XmlConfig;
import pw.phylame.jem.formats.util.config.ConfigUtils;
import pw.phylame.jem.formats.util.config.InvalidConfigException;

/**
 * Config for making PMAB book.
 */
public class PmabMakeConfig extends ZipMakerConfig {
    public static final String CONFIG_SELF = "pmab.make.config";

    public static final String VERSION = "pmab.make.version";
    public static final String TEXT_DIR = "pmab.make.textDir";
    public static final String IMAGE_DIR = "pmab.make.imageDir";
    public static final String EXTRA_DIR = "pmab.make.extraDir";
    public static final String TEXT_ENCODING = "pmab.make.encoding";
    public static final String DATE_FORMAT = "pmab.make.dateFormat";
    public static final String META_INFO = "pmab.make.metaInfo";

    /**
     * Output PMAB version
     */
    public String version = "3.0";

    /**
     * Directory in PMAB for storing text.
     */
    public String textDir = "text";

    /**
     * Directory in PMAB for storing images.
     */
    public String imageDir = "images";

    /**
     * Directory in PMAB for storing extra file(s).
     */
    public String extraDir = "extras";

    /**
     * XML render config.
     */
    public XmlConfig xmlConfig = new XmlConfig();

    /**
     * Encoding for converting all text in PMAB.
     */
    public String textEncoding = PMAB.defaultEncoding;

    /**
     * Format for storing <tt>Date</tt> value.
     */
    public String dateFormat = "yyyy-M-d";

    /**
     * Addition information to PMAB archive.
     * <p><strong>NOTE:</strong> The key and value stored as String.
     */
    public Map<Object, Object> metaInfo = null;

    public PmabMakeConfig() {
        xmlConfig.standalone = true;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void fetch(Map<String, Object> kw) throws InvalidConfigException {
        super.fetch(kw);
        xmlConfig = XmlConfig.fetchInstance(kw);
        xmlConfig.standalone = true;
        version = ConfigUtils.fetchString(kw, VERSION, version);
        textDir = ConfigUtils.fetchString(kw, TEXT_DIR, textDir);
        imageDir = ConfigUtils.fetchString(kw, IMAGE_DIR, imageDir);
        extraDir = ConfigUtils.fetchString(kw, EXTRA_DIR, extraDir);
        textEncoding = ConfigUtils.fetchString(kw, TEXT_ENCODING, textEncoding);
        dateFormat = ConfigUtils.fetchString(kw, DATE_FORMAT, dateFormat);
        metaInfo = ConfigUtils.fetchObject(kw, META_INFO, null, Map.class);
    }
}

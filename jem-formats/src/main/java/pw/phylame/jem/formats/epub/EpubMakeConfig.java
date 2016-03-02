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

package pw.phylame.jem.formats.epub;

import pw.phylame.jem.formats.common.ZipMakeConfig;
import pw.phylame.jem.formats.util.config.ConfigKey;
import pw.phylame.jem.formats.util.html.HtmlConfig;
import pw.phylame.jem.formats.util.xml.XmlConfig;

/**
 * Config for making ePub book.
 */
public class EpubMakeConfig extends ZipMakeConfig {
    public static final String CONFIG_SELF = "epub.make.config";
    public static final String VERSION = "epub.make.version";
    public static final String XML_CONFIG = "epub.make.xmlConfig";
    public static final String HTML_CONFIG = "epub.make.htmlConfig";
    public static final String UUID = "epub.make.uuid";
    public static final String DATE_FORMAT = "epub.make.dateFormat";
    public static final String SMALL_PAGE = "smallPage";

    /**
     * Output ePub version.
     */
    @ConfigKey(VERSION)
    public String version = "2.0";

    @ConfigKey(XML_CONFIG)
    public XmlConfig xmlConfig = new XmlConfig();

    @ConfigKey(HTML_CONFIG)
    public HtmlConfig htmlConfig = new HtmlConfig();

    @ConfigKey(UUID)
    public String uuid = null;

    @ConfigKey(DATE_FORMAT)
    public String dateFormat = EPUB.dateFormat;

    /**
     * If <tt>smallPage</tt> is <tt>true</tt>, each HTML page will be smaller.
     */
    @ConfigKey(SMALL_PAGE)
    public boolean smallPage = true;

    @Override
    public void adjust() {
        xmlConfig.standalone = true;
    }
}

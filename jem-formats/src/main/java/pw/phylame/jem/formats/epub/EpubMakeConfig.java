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

import java.util.Map;

import pw.phylame.jem.formats.common.ZipBookConfig;
import pw.phylame.jem.formats.util.config.ConfigUtils;
import pw.phylame.jem.formats.util.config.InvalidConfigException;
import pw.phylame.jem.formats.util.html.HtmlConfig;
import pw.phylame.jem.formats.util.xml.XmlConfig;

/**
 * Config for making ePub book.
 */
public class EpubMakeConfig extends ZipBookConfig {
    public static final String CONFIG_SELF = "epub.make.config";
    public static final String VERSION = "epub.make.version";
    public static final String UUID = "epub.make.uuid";
    public static final String SMALL_PAGE = "smallPage";

    /**
     * Output ePub version.
     */
    public String version = "2.0";

    public XmlConfig xmlConfig = new XmlConfig();

    public HtmlConfig htmlConfig = new HtmlConfig();

    public String uuid = null;

    public String dateFormat = EPUB.dateFormat;

    /**
     * If <tt>smallPage</tt> is <tt>true</tt>, each HTML page will be smaller.
     */
    public boolean smallPage = true;

    public EpubMakeConfig() {
        xmlConfig.standalone = true;
    }

    @Override
    public void fetch(Map<String, Object> kw) throws InvalidConfigException {
        super.fetch(kw);
        version = ConfigUtils.fetchString(kw, VERSION, version);
        xmlConfig = XmlConfig.fetchInstance(kw);
        xmlConfig.standalone = true;
        htmlConfig = HtmlConfig.fetchInstance(kw);
        uuid = ConfigUtils.fetchString(kw, UUID, uuid);
        smallPage = ConfigUtils.fetchBoolean(kw, SMALL_PAGE, smallPage);
    }
}

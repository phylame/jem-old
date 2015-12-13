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

package pw.phylame.jem.formats.util.html;

import java.util.Map;

import pw.phylame.jem.formats.util.config.ConfigUtils;
import pw.phylame.jem.formats.util.config.CommonConfig;
import pw.phylame.jem.formats.util.config.InvalidConfigException;

/**
 * Config for rendering HTML.
 */
public class HtmlConfig implements CommonConfig {
    public static final String CONFIG_SELF = "html.render.config";
    public static final String ENCODING = "html.render.encoding";
    public static final String INDENT_STRING = "html.render.indentString";

    /**
     * Encoding for writing HTML.
     */
    public String encoding = "UTF-8";

    /**
     * HTML indent string.
     */
    public String indentString = "\t";

    /**
     * Value of attribute xml:lang.
     */
    public String htmlLanguage;

    /**
     * Href of CSS file.
     * <p>You need save CSS file firstly then get the href.
     */
    public String cssHref;

    /**
     * Addition messages to HTML head->meta element.
     */
    public Map<String, String> metaInfo;

    /**
     * HTML CSS config.
     */
    public StyleProvider style;

    /**
     * When making paragraph skip empty line of <tt>TextObject</tt>.
     */
    public boolean skipEmpty = true;

    public static HtmlConfig fetchInstance(Map<String, Object> kw)
            throws InvalidConfigException {
        HtmlConfig config = ConfigUtils.fetchObject(kw, CONFIG_SELF, null,
                HtmlConfig.class);
        if (config != null) {
            return config;
        }
        config = new HtmlConfig();
        config.fetch(kw);
        return config;
    }

    @Override
    public void fetch(Map<String, Object> kw) throws InvalidConfigException {
        encoding = ConfigUtils.fetchString(kw, ENCODING, encoding);
        indentString = ConfigUtils.fetchString(kw, INDENT_STRING, indentString);
    }
}

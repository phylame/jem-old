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

package pw.phylame.jem.formats.jar;

import java.util.Map;

import pw.phylame.jem.formats.common.ZipBookConfig;
import pw.phylame.jem.formats.util.config.ConfigUtils;
import pw.phylame.jem.formats.util.Versions;
import pw.phylame.jem.formats.util.text.TextConfig;
import pw.phylame.jem.formats.util.config.InvalidConfigException;

/**
 * Config for making JAR book.
 */
public class JarMakeConfig extends ZipBookConfig {
    public static final String CONFIG_SELF = "jar.make.config";    // JarMakeConfig
    public static final String VENDOR = "jar.make.vendor"; // String

    /**
     * Render config for rendering book text.
     *
     * @see TextConfig
     */
    public TextConfig textConfig = new TextConfig();

    /**
     * Vendor message of the JAR.
     */
    public String vendor = Versions.VENDOR;

    @Override
    public void fetch(Map<String, Object> kw) throws InvalidConfigException {
        super.fetch(kw);
        textConfig = TextConfig.fetchInstance(kw);
        vendor = ConfigUtils.fetchString(kw, VENDOR, vendor);
    }
}

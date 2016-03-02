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

import pw.phylame.jem.formats.common.ZipMakeConfig;
import pw.phylame.jem.formats.util.Versions;
import pw.phylame.jem.formats.util.text.TextConfig;
import pw.phylame.jem.formats.util.config.ConfigKey;

/**
 * Config for making JAR book.
 */
public class JarMakeConfig extends ZipMakeConfig {
    public static final String CONFIG_SELF = "jar.make.config";
    public static final String TEXT_CONFIG = "jar.make.textConfig";
    public static final String VENDOR = "jar.make.vendor";

    /**
     * Render config for rendering book text.
     *
     * @see TextConfig
     */
    @ConfigKey(TEXT_CONFIG)
    public TextConfig textConfig = new TextConfig();

    /**
     * Vendor message of the JAR.
     */
    @ConfigKey(VENDOR)
    public String vendor = Versions.VENDOR;
}

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

package pw.phylame.jem.formats.umd;

import java.util.List;

import pw.phylame.jem.util.FileObject;
import pw.phylame.jem.formats.util.text.TextConfig;
import pw.phylame.jem.formats.util.config.ConfigKey;
import pw.phylame.jem.formats.util.config.AbstractConfig;

/**
 * Config for making UMD book.
 */
public class UmdMakeConfig extends AbstractConfig {
    public static final String CONFIG_SELF = "umd.make.config";
    public static final String TEXT_CONFIG = "umd.make.textConfig";
    public static final String UMD_TYPE = "umd.make.type";
    public static final String CARTOON_IMAGES = "umd.make.cartoonImages";
    public static final String IMAGE_FORMAT = "umd.make.imageFormat";

    /**
     * Config for rendering book text.
     *
     * @see TextConfig
     */
    @ConfigKey(TEXT_CONFIG)
    public TextConfig textConfig = new TextConfig();

    /**
     * Output UMD type, may be {@link UMD#TEXT}, {@link UMD#CARTOON}, {@link UMD#COMIC}
     */
    @ConfigKey(UMD_TYPE)
    public int umdType = UMD.TEXT;

    /**
     * List of <tt>FileObject</tt> for making {@link UMD#CARTOON} book.
     * The <tt>FileObject</tt> contain image data.
     * <p>If not specify the value, the maker will use covers of each chapter.
     * <p><strong>NOTE:</strong> this value will be available when <tt>umdType</tt>
     * is {@link UMD#CARTOON}.
     */
    @ConfigKey(CARTOON_IMAGES)
    public List<FileObject> cartoonImages = null;

    /**
     * Format of image in <tt>cartoonImages</tt>, ex: jpg, png, bmp..
     */
    @ConfigKey(IMAGE_FORMAT)
    public String imageFormat = "jpg";

    @Override
    public void adjust() {
        textConfig.writeTitle = false;
    }
}

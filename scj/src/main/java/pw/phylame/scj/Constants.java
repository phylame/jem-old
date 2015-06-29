/*
 * Copyright 2014-2015 Peng Wan <phylame@163.com>
 *
 * This file is part of SCJ.
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

package pw.phylame.scj;

/**
 * Constants for SCJ.
 */
public interface Constants {
    String NAME           = "jem";

    String VERSION        = "1.0.3";

    String I18N_PATH = "res/i18n/scj";

    String CHAPTER_REGEX = "^chapter([\\-\\d\\.]+)(\\$.*)?";

    String ITEM_REGEX = "^item\\$.*";

    String VIEW_KEY_ALL = "all";

    String VIEW_KEY_TOC = "toc";

    String VIEW_KEY_EXTENSION = "ext";

    String VIEW_KEY_NAMES = "names";

    String VIEW_KEY_TEXT = "text";

    String VIEW_KEY_SIZE = "size";

    String LINE_SEPARATOR = System.getProperty("line.separator");
}

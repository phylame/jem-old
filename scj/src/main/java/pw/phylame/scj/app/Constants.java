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

package pw.phylame.scj.app;

/**
 * Constants for SCJ.
 */
public interface Constants {
    String NAME = "jem";
    String VERSION = "1.3.1";

    String I18N_NAME = "pw/phylame/scj/i18n/scj";

    String DATE_FORMAT = "yyyy-M-d";

    // view key
    String VIEW_CHAPTER = "^chapter([\\-\\d\\.]+)(\\$.*)?";
    String VIEW_ITEM = "^item\\$.*";
    String VIEW_ALL = "all";
    String VIEW_TOC = "toc";
    String VIEW_EXTENSION = "ext";
    String VIEW_NAMES = "names";
    String VIEW_TEXT = "text";
    String VIEW_SIZE = "size";

    // debug level
    String DEBUG_NONE = "none";
    String DEBUG_ECHO = "echo";
    String DEBUG_TRACE = "trace";

    // CLI options
    String OPTION_HELP = "h";
    String OPTION_VERSION = "v";
    String OPTION_LIST = "l";
    String OPTION_DEBUG_LEVEL = "d";
    String OPTION_INPUT_FORMAT = "f";
    String OPTION_OUTPUT_FORMAT = "t";
    String OPTION_OUTPUT = "o";
    String OPTION_EXTRACT = "x";
    String OPTION_VIEW = "w";
    String OPTION_JOIN = "j";
    String OPTION_CONVERT = "c";
    String OPTION_ATTRIBUTES = "a";
    String OPTION_EXTENSIONS = "e";
    String OPTION_PARSE_ARGUMENTS = "p";
    String OPTION_MAKE_ARGUMENTS = "m";
    String OPTION_LIST_NOVELS = "N";
    String OPTION_LIST_NOVELS_LONG = "novels";
}

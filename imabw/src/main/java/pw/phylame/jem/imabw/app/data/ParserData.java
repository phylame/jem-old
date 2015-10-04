/*
 * Copyright 2015 Peng Wan <phylame@163.com>
 *
 * This file is part of Imabw.
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

package pw.phylame.jem.imabw.app.data;

import java.io.File;
import java.util.Map;

import pw.phylame.jem.core.BookHelper;
import pw.phylame.jem.imabw.app.Worker;
import org.apache.commons.io.FilenameUtils;

/**
 * Warps arguments of Jem parser.
 */
public class ParserData {
    public File                 file;
    public String               format;
    public Map<String, Object>  kw;

    public ParserData(File file) {
        this.file = file;
        this.format = detectFormat(file);
        this.kw = Worker.getInstance().getDefaultParseArguments(format);
    }

    public ParserData(File file, String format, Map<String, Object> kw) {
        this.file = file;
        if (format == null || format.isEmpty()) {
            this.format = detectFormat(file);
        } else {
            this.format = format;
        }
        this.kw = kw;
    }

    private String detectFormat(File file) {
        String ext = FilenameUtils.getExtension(file.getPath()).toLowerCase();
        String format = BookHelper.getFormat(ext);
        return format != null ? format : ext;
    }
}

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

import pw.phylame.jem.core.Book;
import pw.phylame.jem.core.Maker;
import pw.phylame.jem.util.JemException;

import java.io.File;
import java.io.IOException;
import java.util.Map;

/**
 * <tt>Maker</tt> implement for UMD book.
 */
public class UmdMaker implements Maker {
    @Override
    public String getName() {
        return "umd";
    }

    @Override
    public void make(Book book, File file, Map<String, Object> kw) throws IOException, JemException {
        throw new JemException("under development");
    }
}

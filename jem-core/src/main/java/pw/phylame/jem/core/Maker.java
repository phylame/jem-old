/*
 * Copyright 2014-2016 Peng Wan <phylame@163.com>
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

package pw.phylame.jem.core;

import java.util.Map;
import java.io.File;
import java.io.IOException;

import pw.phylame.jem.util.JemException;

/**
 * <tt>Maker</tt> used for making <tt>Book</tt> to book file.
 * <p><strong>NOTE: </strong> the instance of <tt>Maker</tt> be reusable.
 */
public interface Maker {
    /**
     * Returns the format name (normally the extension name).
     *
     * @return the name of format
     */
    String getName();

    /**
     * Writes <tt>Book</tt> to book file.
     *
     * @param book      the <tt>Book</tt> to be written
     * @param file      output file to store book
     * @param arguments arguments to the maker
     * @throws IOException  if occurs I/O errors
     * @throws JemException if occurs errors when making book file
     */
    void make(Book book, File file, Map<String, Object> arguments) throws IOException, JemException;
}

/*
 * Copyright 2015 Peng Wan
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

package pw.phylame.jem.formats.pmab;

import pw.phylame.jem.core.Book;
import pw.phylame.jem.core.Parser;
import pw.phylame.jem.util.JemException;

import java.util.Map;

import java.io.File;
import java.io.IOException;

/**
 * <tt>Parser</tt> implement for PMAB book.
 */
public class PmabParser implements Parser {
    /**
     * Returns the format name(normally the extension name).
     */
    @Override
    public String getName() {
        return "pmab";
    }

    /**
     * Parses book file and stores content as <tt>Book</tt>.
     *
     * @param file the book file
     * @param kw   arguments to the parser
     * @return <tt>Book</tt> represents the book file
     * @throws java.io.IOException              occurs IO errors
     * @throws pw.phylame.jem.util.JemException occurs errors when parsing book file
     */
    @Override
    public Book parse(File file, Map<String, Object> kw) throws IOException, JemException {
        throw new JemException("under development");
    }
}

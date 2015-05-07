/*
 * Copyright 2015 Peng Wan <phylame@163.com>
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

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Map;
import pw.phylame.jem.util.JemException;

/**
 * <tt>Parser</tt> used for parsing book file and stores to <tt>Book</tt>.
 */
public interface Parser {
    /**
     * Returns the format name (normally the extension name).
     * @return the name of format
     */
    String getName();

    /**
     * Parses book file and stores content as <tt>Book</tt>.
     * @param file the book file
     * @param kw arguments to the parser
     * @return <tt>Book</tt> represents the book file
     * @throws IOException occurs IO errors
     * @throws JemException occurs errors when parsing book file
     */
    Book parse(File file, Map<String, Object> kw) throws IOException, JemException;

    /**
     * Parsers book from URL and stores to <tt>Book</tt>.
     * @param source source of book file, maybe URL or file name path
     * @param kw arguments to the parser
     * @return <tt>Book</tt> represents the book file
     * @throws IOException occurs IO errors
     * @throws JemException occurs errors when parsing book file
     * @since 2.0.1
     */
    Book parse(String source, Map<String, Object> kw) throws IOException, JemException;
}

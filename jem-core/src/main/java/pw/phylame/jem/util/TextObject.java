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

package pw.phylame.jem.util;

import java.io.Writer;
import java.io.IOException;
import java.util.List;

/**
 * <tt>TextObject</tt> provides unicode source.
 */
public interface TextObject {

    /**
     * Type for plain text.
     */
    String PLAIN = "plain";

    /**
     * Type for HTML text.
     */
    String HTML = "html";

    /**
     * Returns type of text content.
     *
     * @return the type
     */
    String getType();

    /**
     * Returns text content of this object.
     *
     * @return the string of text, never <tt>null</tt>
     * @throws Exception if failed to get text
     */
    String getText() throws Exception;

    /**
     * Returns list of lines split from text content in this object.
     *
     * @param skipEmpty <tt>true</tt> to skip empty line
     * @return list of lines, never <tt>null</tt>
     * @throws Exception if failed to get lines
     */
    List<String> getLines(boolean skipEmpty) throws Exception;

    /**
     * Writes all text content in this object to output writer.
     *
     * @param writer output <tt>Writer</tt> to store text content
     * @throws IOException occurs IO errors
     */
    void writeTo(Writer writer) throws IOException;
}

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

package pw.phylame.jem.formats.common;

import java.util.Map;
import java.io.File;
import java.io.Closeable;
import java.io.IOException;
import java.io.FileNotFoundException;

import pw.phylame.jem.core.Book;
import pw.phylame.jem.core.Parser;
import pw.phylame.jem.util.JemException;
import pw.phylame.jem.formats.util.SourceCleaner;
import pw.phylame.jem.formats.util.ExceptionFactory;
import pw.phylame.jem.formats.util.ParserException;
import pw.phylame.jem.formats.util.config.CommonConfig;

/**
 * Common parser for e-book file.
 */
public abstract class CommonParser<IN extends Closeable, CF extends CommonConfig>
        extends BookWorker<CF> implements Parser {
    /**
     * The input file to parse.
     * <p>This value will be accessible after {@link #validateInput(Closeable, CommonConfig)}
     */
    protected File source;

    protected CommonParser(String name, String configKey, Class<CF> configClass) {
        super(name, configKey, configClass);
    }

    // 2
    protected abstract IN openInput(File file, CF config) throws IOException;

    // 3

    /**
     * Validates the input source is valid e-book file.
     * <p>This method has no return value, but raise <tt>ParserException</tt>
     * if file is invalid.
     *
     * @param input  the input file
     * @param config the parser config
     * @throws IOException     if occurs I/O error
     * @throws ParserException if the input is invalid
     */
    protected void validateInput(IN input, CF config) throws IOException, ParserException {

    }

    // 4
    protected abstract Book parse(IN input, CF config) throws IOException, ParserException;

    protected ParserException parserException(String msg, Object... args) {
        return ExceptionFactory.parserException(msg, args);
    }

    protected ParserException parserException(Throwable cause, String msg, Object... args) {
        return ExceptionFactory.parserException(cause, msg, args);
    }

    @Override
    public final Book parse(File file, Map<String, Object> arguments) throws IOException, JemException {
        if (!file.exists()) {
            throw new FileNotFoundException("No such file or directory: " + file);
        }
        CF config = fetchConfig(arguments);
        IN input = openInput(file, config);
        if (input == null) {
            throw new AssertionError("Implementation of \"IN openInput(File file, CF config)\" must return valid input");
        }
        Book book;
        try {
            validateInput(input, config);
            source = file;
            book = parse(input, config);
        } catch (IOException | JemException | RuntimeException ex) {
            input.close();
            throw ex;
        }
        if (book == null) {
            throw new AssertionError("Implementation of \"Book parse(IN input, CF config)\" must return valid book");
        }
        book.registerCleanup(new SourceCleaner(input));
        return book;
    }
}

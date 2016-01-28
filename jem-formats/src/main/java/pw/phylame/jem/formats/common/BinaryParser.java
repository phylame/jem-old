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

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

import pw.phylame.jem.formats.util.ParserException;
import pw.phylame.jem.formats.util.BufferedRandomAccessFile;
import pw.phylame.jem.formats.util.config.CommonConfig;

import static pw.phylame.jem.formats.util.ByteUtils.littleParser;

/**
 * Common parser for binary e-book file.
 */
public abstract class BinaryParser<CF extends CommonConfig> extends CommonParser<RandomAccessFile, CF> {
    protected BinaryParser(String name, String configKey, Class<CF> configClass) {
        super(name, configKey, configClass);
    }

    @Override
    protected RandomAccessFile openInput(File file, CF config) throws IOException {
        return new BufferedRandomAccessFile(file, "r");
    }

    /**
     * Invoked if failed to read file content.
     *
     * @throws ParserException raise Jem error
     */
    protected abstract void onReadingError() throws ParserException;

    protected byte[] readBytes(RandomAccessFile input, int size) throws IOException, ParserException {
        return readBytes(input, size, null);
    }

    protected byte[] readBytes(RandomAccessFile input, int size, String errorKey) throws IOException, ParserException {
        byte[] b = new byte[size];
        if (input.read(b) != size) {
            if (errorKey == null) {
                onReadingError();
            } else {
                throw parserException(errorKey);
            }
        }
        return b;
    }

    protected long readUInt32(RandomAccessFile input) throws IOException, ParserException {
        return littleParser.getUInt32(readBytes(input, 4), 0);
    }

    protected int readUInt16(RandomAccessFile input) throws IOException, ParserException {
        return littleParser.getUInt16(readBytes(input, 2), 0);
    }
}

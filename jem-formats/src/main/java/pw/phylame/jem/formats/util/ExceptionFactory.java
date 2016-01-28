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

package pw.phylame.jem.formats.util;

import pw.phylame.jem.formats.util.config.InvalidConfigException;

import java.io.IOException;

public final class ExceptionFactory {
    private ExceptionFactory() {
    }

    public static IOException ioException(String msg, Object... args) {
        return new IOException(MessageBundle.getText(msg, args));
    }

    public static ParserException parserException(String msg, Object... args) {
        return new ParserException(MessageBundle.getText(msg, args));
    }

    public static ParserException parserException(Throwable cause, String msg, Object... args) {
        return new ParserException(MessageBundle.getText(msg, args), cause);
    }

    public static MakerException makerException(String msg, Object... args) {
        return new MakerException(MessageBundle.getText(msg, args));
    }

    public static MakerException makerException(Throwable cause, String msg, Object... args) {
        return new MakerException(MessageBundle.getText(msg, args), cause);
    }

    public static InvalidConfigException invalidObjectArgument(String key, Object o, String className) {
        String msg = MessageBundle.getText("error.config.invalidObject", key, o, className);
        return new InvalidConfigException(key, o, msg);
    }

    public static InvalidConfigException invalidObjectArgument(Throwable cause, String key, Object o,
                                                               String className) {
        String msg = MessageBundle.getText("error.config.invalidObject", key, o, className);
        return new InvalidConfigException(key, o, msg, cause);
    }
}

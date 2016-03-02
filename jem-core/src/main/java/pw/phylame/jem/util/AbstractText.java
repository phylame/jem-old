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

package pw.phylame.jem.util;

import java.util.List;
import java.io.Writer;

/**
 * Abstract class for <tt>TextObject</tt>.
 */
public abstract class AbstractText implements TextObject {
    private final String type;

    protected AbstractText(String type) {
        if (type == null) {
            throw new NullPointerException();
        }
        this.type = type;
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public List<String> getLines(boolean skipEmpty) throws Exception {
        return TextFactory.splitLines(getText(), skipEmpty);
    }

    @Override
    public int writeTo(Writer writer) throws Exception {
        String text = getText();
        writer.write(text);
        writer.flush();
        return text.length();
    }

    @Override
    public String toString() {
        try {
            return getText();
        } catch (Exception e) {
            return "";
        }
    }
}

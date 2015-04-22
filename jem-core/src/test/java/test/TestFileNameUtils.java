/*
 * Copyright 2014-2015 Peng Wan <phylame@163.com>
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

package test;

import junit.framework.TestCase;
import pw.phylame.tools.file.FileNameUtils;

public class TestFileNameUtils extends TestCase {
    public void testExtensionName() {
        assertEquals("txt", FileNameUtils.extensionName("xyz/abc.txt"));
        assertEquals("txt", FileNameUtils.extensionName("abc.txt"));
        assertEquals("txt", FileNameUtils.extensionName(".txt"));
        assertEquals("", FileNameUtils.extensionName("abc"));
        assertEquals("", FileNameUtils.extensionName(""));
    }

    public void testBaseName() {
        assertEquals("abc", FileNameUtils.baseName("xyz/abc.txt"));
        assertEquals("efg", FileNameUtils.baseName("efg.txt"));
        assertEquals("hij", FileNameUtils.baseName("hij."));
        assertEquals("klm", FileNameUtils.baseName("klm"));
        assertEquals("opq", FileNameUtils.baseName("klm/opq"));
        assertEquals("", FileNameUtils.baseName(""));
    }

    public void testMime() {
        assertEquals("image/png", FileNameUtils.getMimeType("abc.png"));
        assertEquals("text/plain", FileNameUtils.getMimeType("abc.txt"));
    }
}

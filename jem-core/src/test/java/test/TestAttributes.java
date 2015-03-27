/*
 * Copyright 2014-2015 Peng Wan <phylame@163.com>
 *
 * This file is part of Jem test suite.
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
import pw.phylame.jem.util.Attributes;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;

public class TestAttributes extends TestCase {
    public void testSize() {
        Attributes attrs = new Attributes();
        assertEquals(attrs.attributeSize(), 0);
    }

    public void testSet() {
        Attributes attrs = new Attributes();
        attrs.setAttribute("name", "PW");
        attrs.setAttribute("age", 21);
        attrs.setAttribute("date", new Date());
        assertEquals(attrs.attributeSize(), 3);
    }

    public void testUpdate() {
        Attributes attrs = new Attributes();
        HashMap<String, Object> maps = new HashMap<String, Object>();
        maps.put("name", "pw");
        maps.put("age", 21);
        maps.put("date", new Date());
        attrs.updateAttributes(maps);
        assertEquals(attrs.attributeSize(), 3);
        Attributes other = new Attributes();
        other.updateAttributes(attrs);
        assertEquals(other.attributeSize(), 3);
    }

    public void testHas() {
        Attributes attrs = new Attributes();
        attrs.setAttribute("name", "PW");
        attrs.setAttribute("age", 21);
        attrs.setAttribute("date", new Date());
        assertTrue(attrs.hasAttribute("name"));
        assertTrue(attrs.hasAttribute("age"));
        assertTrue(attrs.hasAttribute("date"));
        assertFalse(attrs.hasAttribute("where"));
    }

    public void testGet() {
        Attributes attrs = new Attributes();
        attrs.setAttribute("name", "PW");
        attrs.setAttribute("age", 21);
        Date date = new Date();
        attrs.setAttribute("date", date);
        assertNotNull(attrs.getAttribute("age", null));
        assertEquals(attrs.getAttribute("name", null), "PW");
        assertEquals(attrs.getAttribute("date", null), date);
        assertNull(attrs.getAttribute("some", null));
    }

    public void testGetString() {
        Attributes attrs = new Attributes();
        attrs.setAttribute("name", "PW");
        attrs.setAttribute("age", 21);
        assertEquals(attrs.stringAttribute("name", null), "PW");
        assertEquals(attrs.stringAttribute("age", ""), "21");
    }

    public void testRemove() {
        Attributes attrs = new Attributes();
        attrs.setAttribute("name", "PW");
        attrs.setAttribute("age", 21);
        assertEquals(attrs.removeAttribute("name"), "PW");
        assertEquals(attrs.attributeSize(), 1);
        assertNull(attrs.removeAttribute("som"));
        assertEquals(attrs.attributeSize(), 1);
    }

    public void testNames() {
        Attributes attrs = new Attributes();
        attrs.setAttribute("name", "PW");
        attrs.setAttribute("age", 21);
        Collection<String> names = attrs.attributeNames();
        assertEquals(names.size(), 2);
        assertTrue(names.contains("name"));
        assertTrue(names.contains("age"));
    }

    public void testClear() {
        Attributes attrs = new Attributes();
        attrs.setAttribute("name", "PW");
        attrs.setAttribute("age", 21);
        attrs.clearAttributes();
        assertEquals(attrs.attributeSize(), 0);
    }
}

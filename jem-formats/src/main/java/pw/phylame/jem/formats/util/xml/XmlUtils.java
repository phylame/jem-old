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

package pw.phylame.jem.formats.util.xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlSerializer;
import org.xmlpull.v1.XmlPullParserFactory;
import org.xmlpull.v1.XmlPullParserException;
import pw.phylame.jem.formats.util.ExceptionFactory;
import pw.phylame.jem.formats.util.MakerException;
import pw.phylame.jem.formats.util.ParserException;
import pw.phylame.jem.formats.util.text.TextUtils;

/**
 * XML utilities.
 */
public final class XmlUtils {
    private XmlUtils() {
    }

    public static XmlPullParser newPullParser() throws ParserException {
        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(false);
            return factory.newPullParser();
        } catch (XmlPullParserException e) {
            throw ExceptionFactory.parserException(e, "error.xml.getXPP");
        }
    }

    public static XmlSerializer newSerializer() throws MakerException {
        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            return factory.newSerializer();
        } catch (XmlPullParserException e) {
            throw ExceptionFactory.makerException(e, "error.xml.getSerializer");
        }
    }

    public static String getAttribute(XmlPullParser xpp, String name) throws ParserException {
        String str = xpp.getAttributeValue(null, name);
        if (!TextUtils.isValid(str)) {
            throw ExceptionFactory.parserException("error.xml.noAttribute", name, xpp.getName());
        }
        return str;
    }
}

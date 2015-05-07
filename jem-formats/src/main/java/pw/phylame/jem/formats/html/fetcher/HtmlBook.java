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

package pw.phylame.jem.formats.html.fetcher;

import pw.phylame.jem.core.Book;

import java.net.URL;

public class HtmlBook extends Book {
    public static final String URL = "url";
    public static final String FINISHED = "finished";
    public static final String UPDATE_TITLE = "update_title";
    public static final String UPDATE_DATE = "update_date";
    public static final String UPDATE_URL = "update_url";

    public HtmlBook(URL url) {
        super();
        setURL(url);
    }

    public java.net.URL getURL() {
        Object o = getAttribute(URL);
        if (o instanceof URL) {
            return (URL) o;
        }
        return null;
    }

    public void setURL(URL url) {
        if (url == null) {
            throw new NullPointerException("url");
        }
        setAttribute(URL, url);
    }
}

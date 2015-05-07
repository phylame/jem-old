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

package pw.phylame.jem.formats.html;

import pw.phylame.jem.core.Book;
import pw.phylame.jem.core.Parser;
import pw.phylame.jem.formats.html.fetcher.BookFetcher;
import pw.phylame.jem.formats.html.fetcher.HtmlBook;
import pw.phylame.jem.formats.html.fetcher.HtmlChapter;
import pw.phylame.jem.util.JemException;
import pw.phylame.tools.TextObject;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Map;

public class HtmlParser implements Parser {
    @Override
    public String getName() {
        return "online";
    }

    @Override
    public Book parse(File file, Map<String, Object> kw) throws IOException, JemException {
        throw new JemException("unsupported parse from File");
    }

    @Override
    public Book parse(String path, Map<String, Object> kw) throws IOException, JemException {
        if (kw == null || kw.size() == 0) {
            throw new JemException("not found BookFetcher named \"book_fetcher\" in parse " +
                    "argument kw");
        }
        Object o = kw.get("book_fetcher");
        if (! (o instanceof BookFetcher)) {
            throw new JemException("not found BookFetcher in parse argument kw");
        }
        BookFetcher fetcher = (BookFetcher) o;
        URL url = new URL(path);

        return parse(url, fetcher);
    }

    public Book parse(URL url, BookFetcher fetcher) throws IOException, JemException {

        HtmlBook book = fetcher.fetchBook(url);
        for (HtmlChapter chapter: fetcher.fetchContents(book)) {
            TextObject source = fetcher.loadText(chapter);
            source.setType(TextObject.HTML);
            chapter.setSource(source);

            book.append(chapter);
        }

        return book;
    }
}

/*
 * Copyright 2015 Peng Wan <phylame@163.com>
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

package pw.phylame.imabw;

import pw.phylame.jem.core.Book;
import pw.phylame.tools.file.FileFactory;

import java.net.URL;
import java.util.Calendar;

/**
 * The worker.
 */
public class Worker {
    private Application app = Application.getApplication();

    private void addAttributes(Book book) {
        if (book.getCover() == null) {
            URL url = Worker.class.getResource("/cover.png");
            if (url != null) {
                book.setCover(FileFactory.getFile(url, null));
            }
        }
        if (! book.hasAttribute("vendor")) {
            book.setAttribute("vendor", String.format("%s v%s", app.getText("App.Name"), Constants.VERSION));
        }
        if ("".equals(book.getRights())) {
            book.setRights(String.format("(C) %d PW arts", Calendar.getInstance().get(Calendar.YEAR)));
        }
    }

    public Book newBook() {
        Book book = new Book(app.getText("Common.NewBookTitle"), "");
        addAttributes(book);
        book.newChapter(app.getText("Common.NewChapterTitle"));
        return book;
    }

}

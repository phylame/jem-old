/*
 * Copyright 2014-2015 Peng Wan <phylame@163.com>
 *
 * This file is part of SCJ.
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

package pw.phylame.scj.app;

import java.net.URL;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import pw.phylame.jem.core.*;
import pw.phylame.jem.util.*;
import pw.phylame.jem.formats.pmab.PmabMakeConfig;
import pw.phylame.jem.formats.util.MakerException;
import pw.phylame.jem.formats.util.ParserException;

/**
 * Utility class for SCJ.
 */
public final class Worker implements Constants {
    private static SCI app = SCI.sharedInstance();

    static Map<String, Object> propertiesToMap(Properties prop) {
        HashMap<String, Object> map = new HashMap<>();
        if (prop != null) {
            for (Object o : prop.keySet()) {
                map.put((String) o, prop.get(o));
            }
        }
        return map;
    }

    static class InputOption {
        File file;
        String format;
        Map<String, Object> arguments;
        Map<String, Object> attributes;
        Map<String, Object> extensions;

        InputOption(Map<String, Object> context) {
            format = (String) context.getOrDefault(OPTION_INPUT_FORMAT, null);
            arguments = propertiesToMap((Properties) context.get(OPTION_PARSE_ARGUMENTS));
            attributes = propertiesToMap((Properties) context.get(OPTION_ATTRIBUTES));
            extensions = propertiesToMap((Properties) context.get(OPTION_EXTENSIONS));
        }
    }

    static class OutputOption {
        File file;
        String format;
        Map<String, Object> arguments;

        OutputOption(Map<String, Object> context) {
            file = new File((String) context.getOrDefault(OPTION_OUTPUT, "."));
            format = (String) context.getOrDefault(OPTION_OUTPUT_FORMAT,
                    AppConfig.sharedInstance().getOutputFormat());
            arguments = propertiesToMap((Properties) context.get(OPTION_MAKE_ARGUMENTS));
        }
    }

    private static void printJemError(JemException e, File file, String format) {
        if (e instanceof ParserException) {
            app.localizedError(e, "error.jem.parse", file, format.toUpperCase());
        } else if (e instanceof MakerException) {
            app.localizedError(e, "error.jem.make", file, format.toUpperCase());
        } else if (e instanceof UnsupportedFormatException) {
            app.localizedError(e, "error.jem.unsupported", format);
        }
    }

    static Book openBook(InputOption option) {
        Book book = null;
        try {
            book = Jem.readBook(option.file, option.format, option.arguments);
        } catch (IOException e) {
            app.localizedError(e, "error.loadFile", option.file);
        } catch (JemException e) {
            printJemError(e, option.file, option.format);
        }
        return book;
    }

    private static URL detectURL(String url) throws IOException {
        String href;
        if (url.matches("((http://)|(https://)|(ftp://)|(file://)).*")) {
            href = url;
        } else {
            href = "file:///" + new File(url).getAbsolutePath();
        }
        return new URL(href);
    }

    static boolean setAttributes(Chapter chapter, Map<String, Object> attributes) {
        for (String key : attributes.keySet()) {
            String str = (String) attributes.get(key);
            Object value = null;
            if (str.isEmpty()) {
                chapter.removeAttribute(key);
            } else if (Chapter.COVER.equals(key)) {
                try {
                    value = FileFactory.fromURL(detectURL(str), null);
                } catch (IOException e) {
                    app.localizedError(e, "sci.attribute.cover.invalid", str);
                    return false;
                }
            } else if (Chapter.DATE.equals(key)) {
                try {
                    value = new SimpleDateFormat(DATE_FORMAT).parse(str);
                } catch (ParseException e) {
                    app.localizedError("sci.attribute.date.invalid", str);
                    return false;
                }
            } else if (Chapter.INTRO.equals(key)) {
                value = TextFactory.fromString(str);
            } else {
                value = str;
            }
            if (value != null) {
                chapter.setAttribute(key, value);
            }
        }
        return true;
    }

    static void setExtension(Book book, Map<String, Object> items) {
        for (String key : items.keySet()) {
            String str = (String) items.get(key);
            if (str.isEmpty()) {
                book.removeExtension(key);
            } else {
                book.setExtension(key, str);
            }
        }
    }

    static String saveBook(Book book, OutputOption option) {
        File output = option.file;
        if (output.isDirectory()) {
            output = new File(output, String.format("%s.%s", book.getTitle(),
                    option.format));
        }
        if (option.arguments != null) {
            option.arguments.putIfAbsent(PmabMakeConfig.ZIP_COMMENT,
                    String.format("generated by %s v%s", app.getName(),
                            app.getVersion()));
        }
        String path = null;
        try {
            Jem.writeBook(book, output, option.format, option.arguments);
            path = output.getPath();
        } catch (IOException e) {
            app.localizedError(e, "error.saveFile", option.file);
        } catch (JemException e) {
            printJemError(e, option.file, option.format);
        }
        return path;
    }

    static boolean convertBook(InputOption inputOption, OutputOption outputOption) {
        Book book = openBook(inputOption);
        if (book == null) {
            return false;
        }
        if (!setAttributes(book, inputOption.attributes)) {
            book.cleanup();
            return false;
        }
        setExtension(book, inputOption.extensions);

        String path = saveBook(book, outputOption);
        if (path != null) {
            System.out.println(path);
        }

        book.cleanup();
        return true;
    }

    static boolean joinBook(String[] inputs, InputOption inputOption,
                            OutputOption outputOption) {
        Book book = new Book();
        String initFormat = inputOption.format;
        for (String input : inputs) {
            File file = new File(input);
            // check it exists
            if (!file.exists()) {
                app.localizedError("error.input.notExists", input);
                continue;
            }

            String format = (initFormat != null) ? initFormat : app.formatByExtension(input);
            if (!app.checkInputFormat(format)) {
                continue;
            }

            inputOption.file = file;
            inputOption.format = format;
            Book sub = openBook(inputOption);
            if (sub != null) {
                book.append(sub);
            }
        }
        if (!setAttributes(book, inputOption.attributes)) {
            book.cleanup();
            return false;
        }
        setExtension(book, inputOption.extensions);

        String path = saveBook(book, outputOption);
        if (path != null) {
            System.out.println(path);
        }

        book.cleanup();
        inputOption.format = initFormat;
        return true;
    }

    private static int[] parseIndexes(String str) {
        String[] parts = str.split("\\.");
        int[] indices = new int[parts.length];
        for (int i = 0; i < parts.length; i++) {
            String part = parts[i];
            try {
                int n = Integer.parseInt(part);
                if (n == 0) {
                    app.localizedError("sci.invalidIndexes", str);
                    return null;
                }
                if (n > 0) {
                    --n;
                }
                indices[i] = n;
            } catch (NumberFormatException e) {
                app.localizedError(e, "sci.invalidIndexes", str);
                return null;
            }
        }
        return indices;
    }

    static boolean extractBook(InputOption inputOption, String index,
                               OutputOption outputOption) {
        Book book = openBook(inputOption);
        if (book == null) {
            return false;
        }
        int[] indexes = parseIndexes(index);
        if (indexes == null) {
            book.cleanup();
            return false;
        }
        Chapter chapter = Jem.chapterForIndex(book, indexes);
        if (chapter == null) {
            book.cleanup();
            return false;
        }

        Book outBook = Jem.chapterToBook(chapter);
        if (!chapter.isSection()) {
            outBook.append(new Chapter(app.getText("sci.contentTitle"),
                    chapter.getContent()));
        }

        if (!setAttributes(outBook, inputOption.attributes)) {
            outBook.cleanup();
            book.cleanup();
            return false;
        }
        setExtension(outBook, inputOption.extensions);

        String path = saveBook(outBook, outputOption);
        if (path != null) {
            System.out.println(path);
        }

        outBook.cleanup();
        book.cleanup();
        return true;
    }

    static boolean viewBook(InputOption option, String[] keys) {
        Book book = openBook(option);
        if (book == null) {
            return false;
        }
        if (!setAttributes(book, option.attributes)) {
            book.cleanup();
            return false;
        }
        setExtension(book, option.extensions);

        boolean state = true;
        for (String key : keys) {
            if (key.equals(VIEW_EXTENSION)) {
                String[] names = book.extensionNames();
                state = viewExtension(book, names) && state;
            } else if (key.matches(VIEW_CHAPTER)) {
                state = viewChapter(book, key) && state;
            } else if (key.matches(VIEW_ITEM)) {
                String[] names = new String[]{key.replaceFirst("item\\$", "")};
                state = viewExtension(book, names) && state;
            } else {
                viewAttribute(book, new String[]{key}, System.lineSeparator(), false, false);
            }
        }

        book.cleanup();
        return state;
    }

    private static String formatVariant(String key, Object value) {
        String str = "";
        if (value instanceof FileObject) {
            str = value.toString();
        } else if (value instanceof TextObject) {
            try {
                str = ((TextObject) value).getText();
            } catch (Exception e) {
                app.localizedError(e, "error.view.invalidText");
            }
        } else if (value instanceof Date) {
            str = new SimpleDateFormat(DATE_FORMAT).format((Date) value);
        } else if (value != null) {
            if (key != null) {
                if (key.equals(Book.LANGUAGE)) {
                    String tag = ((String) value).replace('_', '-');
                    str = Locale.forLanguageTag(tag).getDisplayName();
                } else {
                    str = String.valueOf(value);
                }
            } else {
                str = String.valueOf(value);
            }
        }
        return str;
    }

    private static void walkTree(Chapter chapter, String prefix, String[] keys,
                                 boolean showAttributes, boolean showOrder,
                                 String indent, boolean showBrackets) {
        System.out.print(prefix);
        if (showAttributes) {
            viewAttribute(chapter, keys, ", ", showBrackets, true);
        }
        int order = 1;
        String size = String.valueOf(chapter.size());
        String format = "%" + size.length() + "d";
        for (Chapter sub : chapter) {
            String str = prefix;
            if (showAttributes) {
                str += indent;
            }
            if (showOrder) {
                str += String.format(format, order++) + " ";
            }
            walkTree(sub, str, keys, true, showOrder, indent, showBrackets);
        }
    }

    private static void viewToc(Chapter chapter, String[] keys, String indent,
                                boolean showOrder, boolean showBrackets) {
        System.out.println(app.getText("sci.view.tocTitle", chapter.getTitle()));
        walkTree(chapter, "", keys, false, showOrder, indent, showBrackets);
    }

    private static void viewAttribute(Chapter chapter, String[] keys, String sep,
                                      boolean showBrackets, boolean ignoreEmpty) {
        LinkedList<String> lines = new LinkedList<>();
        for (String key : keys) {
            if (key.equals(VIEW_ALL)) {
                String[] names = chapter.attributeNames();
                viewAttribute(chapter, names, sep, showBrackets, true);
            } else if (key.equals(VIEW_TOC)) {
                String[] names = {Chapter.TITLE, Chapter.COVER};
                viewToc(chapter, names, AppConfig.sharedInstance().getTocIndent(), true, true);
            } else if (key.equals(VIEW_TEXT)) {
                try {
                    System.out.println(chapter.getContent().getText());
                } catch (Exception e) {
                    app.localizedError(e, "error.view.fetchContent", chapter.getTitle());
                }
            } else if (key.equals(VIEW_NAMES)) {
                LinkedList<String> names = new LinkedList<>();
                Collections.addAll(names, chapter.attributeNames());
                Collections.addAll(names, VIEW_TEXT, VIEW_SIZE, VIEW_ALL);
                if (chapter.isSection()) {
                    names.add(VIEW_TOC);
                }
                if (chapter instanceof Book) {
                    names.add(VIEW_EXTENSION);
                }
                System.out.println(String.join(", ", names));
            } else if (key.equals(VIEW_SIZE) && !chapter.hasAttribute(VIEW_SIZE)) {
                String str = formatVariant(null, chapter.size());
                if (!"".equals(str)) {
                    lines.add(app.getText("sci.view.attributeFormat", key,
                            "\"" + str + "\""));
                }
            } else {
                Object value = chapter.getAttribute(key, null);
                String str;
                if (value != null) {
                    str = formatVariant(key, value);
                } else {
                    str = "";
                }

                if (!str.isEmpty() || !ignoreEmpty) {
                    lines.add(app.getText("sci.view.attributeFormat", key,
                            "\"" + str + "\""));
                }
            }
        }
        if (lines.size() == 0) {
            return;
        }
        if (showBrackets) {
            System.out.println("<" + String.join(sep, lines) + ">");
        } else {
            System.out.println(String.join(sep, lines));
        }
    }

    private static boolean viewExtension(Book book, String[] names) {
        boolean state = true;
        for (String name : names) {
            Object value = book.getExtension(name, null);
            if (value == null) {
                app.localizedError("error.view.notFoundItem", name);
                state = false;
            } else {
                String str = formatVariant(null, value);
                System.out.println(app.getText("sci.view.extensionFormat", name,
                        Jem.variantType(value), str));
            }
        }
        return state;
    }

    private static boolean viewChapter(Book book, String name) {
        String[] parts = name.replaceFirst("chapter", "").split("\\$");
        String index = parts[0], key;
        if (parts.length > 1) {
            key = parts[1];
        } else {
            key = VIEW_TEXT;
        }
        int[] indexes = parseIndexes(index);
        if (indexes == null) {
            return false;
        }
        try {
            viewAttribute(Jem.chapterForIndex(book, indexes),
                    new String[]{key}, System.lineSeparator(), false, false);
            return true;
        } catch (IndexOutOfBoundsException e) {
            app.localizedError(e, "error.view.notFoundChapter", index, book.getTitle());
            return false;
        }
    }
}

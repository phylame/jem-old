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

package pw.phylame.scj;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import java.util.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import pw.phylame.jem.core.Chapter;
import pw.phylame.jem.core.Jem;
import pw.phylame.jem.core.Part;
import pw.phylame.jem.core.Book;
import pw.phylame.jem.util.FileFactory;
import pw.phylame.jem.util.FileObject;
import pw.phylame.jem.util.JemException;
import pw.phylame.jem.util.TextObject;
import pw.phylame.jem.formats.pmab.PmabMaker;
import pw.phylame.tools.DateUtils;
import pw.phylame.tools.StringUtils;

/**
 * Utility class for SCJ.
 */
public final class Worker implements Constants {
    private static Log LOG = LogFactory.getLog(Worker.class);

    private static SCI app = SCI.getInstance();

    private static URL detectURL(String url) throws IOException {
        String href;
        if (url.matches("((http://)|(https://)|(ftp://)|(file://)).*")) {
            href = url;
        } else {
            href = "file:///" + new File(url).getAbsolutePath();
        }
        return new URL(href);
    }

    private static FileObject getPemCover() {
        URL url = Worker.class.getResource("/cover.png");   // from jem-formats
        if (url != null) {
            return FileFactory.fromURL(url, null);
        }
        return null;
    }

    static boolean setAttributes(Part part, Map<String, Object> attributes) {
        for (String key: attributes.keySet()) {
            String raw = String.valueOf(attributes.get(key));
            Object value = null;
            if (Part.COVER.equals(key)) {
                FileObject cover;
                if ("_pem_cover_".equals(raw)) {
                    cover = getPemCover();
                } else {
                    try {
                        cover = FileFactory.fromURL(detectURL(raw), null);
                    } catch (IOException e) {
                        LOG.debug("invalid cover file: "+raw, e);
                        app.error(app.getText("SCI_INVALID_COVER", raw));
                        return false;
                    }
                }
                if (cover != null) {
                    value = cover;
                }
            } else if (Part.DATE.equals(key)) {
                Date date = DateUtils.parseDate(raw,
                        app.getText("SCI_DATE_FORMAT"), null);
                if (date == null) {
                    app.error(app.getText("SCI_INVALID_DATE", raw));
                    return false;
                } else {
                    value = date;
                }
            } else if (Part.INTRO.equals(key)) {
                value = new TextObject(raw);
            } else {
                value = raw;
            }
            if (value != null) {
                part.setAttribute(key, value);
            }
        }
        return true;
    }

    static void setExtension(Book book, Map<String, Object> items) {
        for (String key: items.keySet()) {
            Object o = items.get(key);
            if (o == null) {
                book.removeItem(key);
            } else if (o instanceof String) {
                String str = (String) o;
                if (str.length() == 0) {
                    book.removeItem(key);
                }
                book.setItem(key, str);
            } else {
                book.setItem(key, o);
            }
        }
    }

    static Book openBook(File input, String format, Map<String, Object> kw) {
        Book book = null;
        try {
            book = Jem.readBook(input, format, kw);
        } catch (IOException | JemException e) {
            LOG.debug(String.format("failed to read '%s' with '%s'", input,
                    format.toUpperCase()), e);
        }
        return book;
    }

    private static void addPmabInfo(Map<String, Object> kw) {
        if (kw == null) {
            return;
        }

        String info = "generated by " + app.getName()+" v"+app.getVersion();
        if (! kw.containsKey(PmabMaker.KEY_COMMENT)) {
            kw.put(PmabMaker.KEY_COMMENT, info);
        }
        if (! kw.containsKey(PmabMaker.KEY_META_DATA)) {
            HashMap<Object, Object> metaInfo = new HashMap<>();
            metaInfo.put("generator", info);
            kw.put(PmabMaker.KEY_META_DATA, metaInfo);
        }
    }

    static String saveBook(Book book, File output, String format,
                           Map<String, Object> kw) {
        if (output.isDirectory()) {
            output = new File(output, String.format("%s.%s", book.getTitle(), format));
        }
        addPmabInfo(kw);
        String path = null;
        try {
            Jem.writeBook(book, output, format, kw);
            path = output.getPath();
        } catch (IOException |JemException e) {
            LOG.debug(String.format("failed to write '%s' with '%s'",
                    output.getPath(), format.toUpperCase()), e);
        }
        return path;
    }

    static String convertBook(File input,
                              String inFormat,
                              Map<String, Object> inKw,
                              Map<String, Object> attributes,
                              Map<String, Object> items,
                              File output,
                              String outFormat,
                              Map<String, Object> outKw) {
        Book book = openBook(input, inFormat, inKw);
        if (book == null) {
            app.error(app.getText("SCI_READ_FAILED", input));
            return null;
        }
        if (! setAttributes(book, attributes)) {
            return null;
        }

        setExtension(book, items);

        String path = saveBook(book, output, outFormat, outKw);
        if (path == null) {
            app.error(app.getText("SCI_CONVERT_FAILED", input, output.getAbsolutePath()));
        }

        book.cleanup();
        return path;
    }

    static String joinBook(List<File> inputs,
                           Map<String, Object> inKw,
                           Map<String, Object> attributes,
                           Map<String, Object> items,
                           File output,
                           String outFormat,
                           Map<String, Object> outKw) {
        Book book = new Book();
        for (File input: inputs) {
            Book sub = openBook(input, null, inKw);
            if (sub == null) {
                app.error(app.getText("SCI_READ_FAILED", input));
            } else {
                book.append(sub);
            }
        }
        if (! setAttributes(book, attributes)) {
            return null;
        }

        setExtension(book, items);

        String path = saveBook(book, output, outFormat, outKw);
        if (path == null) {
            app.error(app.getText("SCI_JOIN_FAILED", output.getAbsolutePath()));
        }

        for (Part sub: book) {
            sub.cleanup();
        }
        book.cleanup();
        return path;
    }

    private static int[] parseIndexes(String indexes) {
        ArrayList<Integer> parts = new ArrayList<>();
        for (String part: indexes.split("\\.")) {
            try {
                int n = new Integer(part);
                if (n == 0) {
                    app.error(app.getText("SCI_INVALID_INDEXES", indexes));
                    return null;
                }
                parts.add(n);
            } catch(NumberFormatException ex) {
                app.error(app.getText("SCI_INVALID_INDEXES", indexes));
                return null;
            }
        }
        int[] results = new int[parts.size()];
        int ix = 0;
        for (int n: parts) {
            if (n > 0) {
                n--;
            }
            results[ix++] = n;
        }
        return results;
    }

    static String extractBook(File input,
                              String inFormat,
                              Map<String, Object> inKw,
                              Map<String, Object> attributes,
                              Map<String, Object> items,
                              String index,
                              File output,
                              String outFormat,
                              Map<String, Object> outKw) {
        Book book = openBook(input, inFormat, inKw);
        if (book == null) {
            app.error(app.getText("SCI_READ_FAILED", input));
            return null;
        }
        int[] indexes = parseIndexes(index);
        if (indexes == null) {
            return null;
        }
        Part part = Jem.getPart(book, indexes, 0);
        if (! setAttributes(part, attributes)) {
            return null;
        }

        Book dest = Jem.toBook(part);
        if (! part.isSection()) {
            dest.append(new Chapter(app.getText("SCI_MAIN_CHAPTER_TITLE"),
                    part.getSource()));
        }
        setExtension(dest, items);

        String path = saveBook(dest, output, outFormat, outKw);
        if (path == null) {
            app.error(app.getText("SCI_EXTRACT_FAILED", index, output.getAbsolutePath()));
        }

        dest.cleanup();
        book.cleanup();
        return path;
    }

    private static String formatVariant(Object value) {
        String str = "";
        if (value instanceof FileObject) {
            str = ((FileObject)value).getName();
        } else if (value instanceof TextObject) {
            TextObject to = (TextObject)value;
            try {
                str = to.getText();
            } catch (IOException ex) {
                LOG.debug("load text of "+to.getFile().getName(), ex);
                app.error(app.getText("SCI_LOAD_TEXT_FAILED", to.getFile().getName()));
            }
        } else if (value instanceof Date) {
            str = DateUtils.formatDate((Date)value, app.getText("SCI_DATE_FORMAT"));
        } else if (value instanceof byte[]) {
            str = Arrays.toString((byte[])value);
        } else if (value instanceof Byte[]) {
            str = Arrays.toString((Byte[])value);
        } else if (value != null) {
            str = String.valueOf(value);
        }
        return str;
    }

    private static void walkTree(Part part,
                                 String prefix,
                                 String[] keys,
                                 boolean showAttributes,
                                 boolean showOrder,
                                 String indent,
                                 boolean showBrackets) {
        System.out.print(prefix);
        if (showAttributes) {
            viewPart(part, keys, ", ", showBrackets, true);
        }
        int order = 1;
        String size = String.valueOf(part.size());
        String format = "%" + size.length() + "d";
        for (Part sub: part) {
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

    private static void viewToc(Part part,
                                String[] keys,
                                String indent,
                                boolean showOrder, boolean showBrackets) {
        System.out.println(app.getText("SCI_TOC_TITLE", part.getTitle()));
        walkTree(part, "", keys, false, showOrder, indent, showBrackets);
    }

    private static void viewPart(Part part,
                                 String[] keys,
                                 String sep,
                                 boolean showBrackets, boolean ignoreEmpty) {
        ArrayList<String> lines = new ArrayList<>();
        for (String key: keys) {
            if (key.equals(VIEW_KEY_ALL)) {
                viewPart(part, part.attributeNames(), sep, showBrackets, true);
            } else if (key.equals(VIEW_KEY_TOC)) {
                viewToc(part, new String[]{Part.TITLE, Part.COVER},
                        app.getConfig().getTocIndent(), true, true);
            } else if (key.equals(VIEW_KEY_TEXT)) {
                try {
                    System.out.println(part.getSource().getText());
                } catch (IOException ex) {
                    LOG.debug("load content source: " +
                            part.getSource().getFile().getName(), ex);
                    app.error(app.getText("SCI_LOAD_CONTENT_FAILED", part.getTitle()));
                }
            } else if (key.equals(VIEW_KEY_NAMES)) {
                ArrayList<String> names = new ArrayList<>();
                Collections.addAll(names, part.attributeNames());
                Collections.addAll(names, VIEW_KEY_TEXT, VIEW_KEY_SIZE, VIEW_KEY_ALL);
                if (part instanceof Book) {
                    names.add(VIEW_KEY_EXTENSION);
                }
                System.out.println(StringUtils.join(names, ", "));
            } else if (key.equals(VIEW_KEY_SIZE) && ! part.hasAttribute(VIEW_KEY_SIZE)) {
                String str = formatVariant(part.size());
                if (! "".equals(str)) {
                    lines.add(app.getText("SCI_ATTRIBUTE_FORMAT", key, "\"" + str + "\""));
                }
            } else {
                Object value = part.getAttribute(key, null);
                String str = null;
                if (! ignoreEmpty) {
                    str = formatVariant(value);
                } else if (value != null) {
                    str = formatVariant(value);
                    str = ! "".equals(str) ? str: null;
                }
                if (str != null) {
                    lines.add(app.getText("SCI_ATTRIBUTE_FORMAT", key, "\"" + str + "\""));
                }
            }
        }
        if (lines.size() == 0) {
            return;
        }
        if (showBrackets) {
            System.out.println("<" + StringUtils.join(lines, sep) + ">");
        } else {
            System.out.println(StringUtils.join(lines, sep));
        }
    }

    private static void viewExtension(Book book, String[] names) {
        for (String name: names) {
            Object value = book.getItem(name, null);
            if (value == null) {
                app.echo(app.getText("SCI_NOT_FOUND_ITEM", name));
            } else {
                String str = formatVariant(value);
                System.out.println(app.getText("SCI_ITEM_FORMAT",
                        name, Jem.variantType(value), str));
            }
        }
    }

    private static void viewChapter(Book book, String name) {
        String[] parts = name.replaceFirst("chapter", "").split("\\$");
        String index = parts[0], key = VIEW_KEY_TEXT;
        if (parts.length > 1) {
            key = parts[1];
        }
        int[] indexes = parseIndexes(index);
        if (indexes == null) {
            return;
        }
        try {
            Part part = Jem.getPart(book, indexes, 0);
            viewPart(part, new String[]{key}, LINE_SEPARATOR, false, false);
        } catch (IndexOutOfBoundsException ex) {
            app.error(app.getText("SCI_NOT_FOUND_CHAPTER", index, book.getTitle()));
        }
    }

    static boolean viewBook(File input,
                            String inFormat,
                            Map<String, Object> inKw,
                            Map<String, Object> attributes,
                            Map<String, Object> items,
                            String[] keys) {
        Book book = openBook(input, inFormat, inKw);
        if (book == null) {
            app.error(app.getText("SCI_READ_FAILED", input));
            return false;
        }
        if (! setAttributes(book, attributes)) {
            return false;
        }

        setExtension(book, items);

        for (String key: keys) {
            if (key.equals(VIEW_KEY_EXTENSION)) {
                viewExtension(book, book.itemNames());
            } else if (key.matches(CHAPTER_REGEX)) {
                viewChapter(book, key);
            } else if (key.matches(ITEM_REGEX)) {
                viewExtension(book, new String[]{key.replaceFirst("item\\$", "")});
            } else {
                viewPart(book, new String[]{key}, LINE_SEPARATOR, false, false);
            }
        }

        book.cleanup();
        return true;
    }
}

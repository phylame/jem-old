/*
 * Copyright 2015 Peng Wan <phylame@163.com>
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
import java.util.Map;
import java.util.List;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import pw.phylame.jem.core.Jem;
import pw.phylame.jem.core.Part;
import pw.phylame.jem.core.Book;
import pw.phylame.jem.util.JemException;
import pw.phylame.tools.DateUtils;
import pw.phylame.tools.StringUtils;
import pw.phylame.tools.TextObject;
import pw.phylame.tools.file.FileUtils;
import pw.phylame.tools.file.FileObject;
import pw.phylame.tools.file.FileFactory;

/**
 * Utility class for SCJ.
 */
public final class Worker {
	private static Log LOG = LogFactory.getLog(Worker.class);

	private static final String CHAPTER_REGEX = "^chapter([\\-\\d\\.]+)(\\$.*)?";
	private static final String ITEM_REGEX = "^item\\$.*";

	public static boolean setAttributes(Part part, Map<String, Object> attributes) {
		for (String key: attributes.keySet()) {
			String raw = String.valueOf(attributes.get(key));
			Object value = null;
			if ("cover".equals(key)) {
				File file = new File(raw);
				if (! file.exists()) {
					SCI.error(String.format(SCI.getString("SCI_NOT_EXISTS_COVER"), raw));
					return false;
				} else
					try {
						part.setAttribute("cover", FileFactory.getFile(file, null));
					} catch (Exception ex) {
						SCI.error(ex.getMessage());
						LOG.debug("cannot create FileObject", ex);
						return false;
					}
			} else if ("date".equals(key)) {
				Date date = DateUtils.parseDate(raw, SCI.getString("SCI_DATE_FORMAT"), null);
				if (date == null) {
					SCI.error(String.format(SCI.getString("SCI_INVALID_DATE"), raw));
					return false;
				} else {
					value = date;
				}
			} else if ("intro".equals(key)) {
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

	public static Book openBook(File input, String format, Map<String, Object> kw) {
		if (format == null || "".equals(format)) {
			format = FileUtils.getExtension(input.getPath()).toLowerCase();
		}
		Book book = null;
		try {
			book = Jem.readBook(input, format, kw);
		} catch (IOException e) {
			SCI.error(e.getMessage());
			LOG.debug("reading "+format.toUpperCase(), e);
		} catch (JemException e) {
			SCI.error(e.getMessage());
			LOG.debug("reading "+format.toUpperCase(), e);
		}
		return book;
	}

	public static String saveBook(Book book, File output, String format,
			Map<String, Object> kw) {
		if (output.isDirectory()) {
			output = new File(output, String.format("%s.%s", book.getTitle(), format));
		}
		String path = null;
		try {
			Jem.writeBook(book, output, format, kw);
			path = output.getPath();
		} catch (IOException e) {
			SCI.error(e.getMessage());
			LOG.debug("writing "+format.toUpperCase(), e);
		} catch (JemException e) {
			SCI.error(e.getMessage());
			LOG.debug("writing "+format.toUpperCase(), e);
		}
		return path;
	}

	public static String convertBook(File input, String inFormat,
			Map<String, Object> inKw, Map<String, Object> attributes,
			File output, String outFormat, Map<String, Object> outKw) {
		Book book = openBook(input, inFormat, inKw);
		if (book == null) {
			SCI.error(String.format(SCI.getString("SCI_READ_FAILED"), input.getPath()));
			return null;
		}
		if (! setAttributes(book, attributes)) {
			return null;
		}
		String path = saveBook(book, output, outFormat, outKw);
		if (path == null) {
			SCI.error(String.format(SCI.getString("SCI_CONVERT_FAILED"),
					input.getPath(), output.getPath()));
		}
		return path;
	}

	public static String joinBook(File[] inputs, Map<String, Object> inKw,
			Map<String, Object> attributes, File output, String outFormat,
			Map<String, Object> outKw) {
		Book book = new Book();
		for (File input: inputs) {
			Book sub = openBook(input, null, inKw);
			if (sub == null) {
				SCI.error(String.format(SCI.getString("SCI_READ_FAILED"), input.getPath()));
			} else {
				book.append(sub);
			}
		}
		if (! setAttributes(book, attributes)) {
			return null;
		}
		String path = saveBook(book, output, outFormat, outKw);
		if (path == null) {
			SCI.error(String.format(SCI.getString("SCI_JOIN_FAILED"), output.getPath()));
		}
		return path;
	}

	private static int[] parseIndexs(String indexs) {
		List<Integer> parts = new java.util.ArrayList<Integer>();
		for (String part: indexs.split("\\.")) {
			try {
				int n = new Integer(part);
				if (n == 0) {
					SCI.error(String.format(SCI.getString("SCI_INVALID_INDEXS"),
							indexs));
					return null;
				}
				parts.add(n);
			} catch(NumberFormatException ex) {
				SCI.error(String.format(SCI.getString("SCI_INVALID_INDEXS"), indexs));
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

	public static String extractBook(File input, String inFormat,
			Map<String, Object> inKw, Map<String, Object> attributes,
			String index, File output, String outFormat, Map<String, Object> outKw) {
		Book book = openBook(input, inFormat, inKw);
		if (book == null) {
			SCI.error(String.format(SCI.getString("SCI_READ_FAILED"),
					input.getPath()));
			return null;
		}
		int[] indexs = parseIndexs(index);
		if (indexs == null) {
			return null;
		}
		Part part = Jem.getPart(book, indexs, 0);
		if (! setAttributes(part, attributes)) {
			return null;
		}
		String path = saveBook(Jem.toBook(part), output, outFormat, outKw);
		if (path == null) {
			SCI.error(String.format(SCI.getString("SCI_EXTRACT_FAILED"), index,
					output.getPath()));
		}
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
				SCI.error(String.format(SCI.getString("SCI_LOAD_TEXT_FAILED"),
						to.getFile().getName()));
				LOG.debug("load text of "+to.getFile().getName(), ex);
			}
		} else if (value instanceof Date) {
			str = DateUtils.formatDate((Date)value, SCI.getString("SCI_DATE_FORMAT"));
		} else if (value != null) {
			str = String.valueOf(value);
		}
		return str;
	}

	private static void walkTree(Part part, String prefix, String[] keys,
			boolean showAttributes, boolean showOrder, String indent) {
		System.out.print(prefix);
		if (showAttributes) {
			viewPart(part, keys, ", ", true, true);
		}
		int order = 1;
		for (Part sub: part) {
			String str = prefix;
			if (showAttributes) {
				str += indent;
			}
			if (showOrder) {
				str += String.valueOf(order++) + " ";
			}
			walkTree(sub, str, keys, true, showOrder, indent);
		}
	}

	private static void viewToc(Part part, String[] keys, String indent,
			boolean showOrder, boolean showBrackets) {
		System.out.println(String.format(SCI.getString("SCI_TOC_TITLE"),
				part.getTitle()));
		walkTree(part, "", keys, false, showOrder, indent);
	}

	private static void viewPart(Part part, String[] keys, String sep,
			boolean showBrackets, boolean ignoreEmpty) {
		List<String> lines = new java.util.ArrayList<String>();
		for (String key: keys) {
			if (key.equals("all")) {
				viewPart(part, part.attributeNames().toArray(new String[0]),
						sep, showBrackets, true);
			} else if (key.equals("toc")) {
				viewToc(part, new String[]{"title", "cover"},
						SCI.getString("SCI_TOC_INDENT"), true, true);
			} else if (key.equals("text")) {
				try {
					System.out.println(part.getSource().getText());
				} catch (IOException ex) {
					ex.printStackTrace();
					LOG.debug("load content source: "+part.getSource().getFile().getName(), ex);
					SCI.error(String.format(SCI.getString("SCI_LOAD_CONTENT_FAILED"),
							part.getTitle()));
				}
			} else if (key.equals("names")) {
				List<String> names = new java.util.ArrayList<String>(
						part.attributeNames());
				names.addAll(java.util.Arrays.asList("text", "size", "all"));
				if (part instanceof Book) {
					names.add("ext");
				}
				System.out.println(StringUtils.join(names, ", "));
			} else if (key.equals("size") && ! part.hasAttribute("size")) {
				String str = formatVariant(part.size());
				if (! "".equals(str)) {
					lines.add(String.format(SCI.getString("SCI_ATTRIBUTE_FORMAT"),
							key, "\"" + str + "\""));
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
                    lines.add(String.format(SCI.getString("SCI_ATTRIBUTE_FORMAT"), key,
                                "\"" + str + "\""));
                }
			}
		}
		if (lines.size() == 0) {
			return;
		}
		if (showBrackets) {
			System.out.println("<"+StringUtils.join(lines, sep)+">");
		} else {
			System.out.println(StringUtils.join(lines, sep));
		}
	}

	private static void viewExtension(Book book, String[] names) {
		for (String name: names) {
			Object value = book.getItem(name, null);
			if (value == null) {
				SCI.echo(String.format(SCI.getString("SCI_NOT_FOUND_ITEM"), name));
			} else {
				String str = formatVariant(value);
				System.out.println(String.format(SCI.getString("SCI_ITEM_FORMAT"),
						name, Jem.variantType(value), str));
			}
		}
	}

	private static void viewChapter(Book book, String name) {
		String[] parts = name.replaceFirst("chapter", "").split("\\$");
		String index = parts[0], key = "text";
		if (parts.length > 1) {
			key = parts[1];
		}
		int[] indexs = parseIndexs(index);
		if (indexs == null) {
			return;
		}
		try {
			Part part = Jem.getPart(book, indexs, 0);
			viewPart(part, new String[]{key}, System.getProperty("line.separator"),
					false, false);
		} catch (IndexOutOfBoundsException ex) {
			SCI.error(String.format(SCI.getString("SCI_NOT_FOUND_CHAPTER"),
					index, book.getTitle()));
		}
	}

	public static boolean viewBook(File input, String inFormat, Map<String, Object> inKw,
			Map<String, Object> attributes, String[] keys) {
		Book book = openBook(input, inFormat, inKw);
		if (book == null) {
			SCI.error(String.format(SCI.getString("SCI_READ_FAILED"),
					input.getPath()));
			return false;
		}
		if (! setAttributes(book, attributes)) {
			return false;
		}
		for (String key: keys) {
			if (key.equals("ext")) {
				viewExtension(book, book.itemNames().toArray(new String[0]));
			} else if (key.matches(CHAPTER_REGEX)) {
				viewChapter(book, key);
			} else if (key.matches(ITEM_REGEX)) {
				viewExtension(book, new String[]{key.replaceFirst("item\\$", "")});
			} else {
				viewPart(book, new String[]{key},
						System.getProperty("line.separator"), false, false);
			}
		}
		return true;
	}
}

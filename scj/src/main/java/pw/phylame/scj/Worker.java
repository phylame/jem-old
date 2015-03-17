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
import java.io.InputStream;
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
import pw.phylame.tools.NumberUtils;
import pw.phylame.tools.StringUtils;
import pw.phylame.tools.TextObject;
import pw.phylame.tools.file.FileUtils;
import pw.phylame.tools.file.FileObject;

/**
 * Utility class for SCJ.
 */
public final class Worker {
	private static Log LOG = LogFactory.getLog(Worker.class);

	private static final String CHAPTER_REGEX = "^chapter[\\d]+\\$.*";
	private static final String ITEM_REGEX = "^item[\\d]+\\$.*";

	public static void setAttributes(Book book, Map<String, Object> attributes) {
		for (String key: attributes.keySet()) {
			String value = String.valueOf(attributes.get(key));
			if ("cover".equals(key)) {
				File file = new File(value);
				if (! file.exists()) {
					SCI.error(String.format(SCI.getString("SCI_NOT_EXISTS_COVER"),
							value));
				} else {
					// TODO: set cover to book using FileObject
				}
			} else if ("date".equals(key)) {
				Date date = DateUtils.parseDate(value,
						SCI.getString("SCI_DATE_FORMAT"), null);
				if (date != null) {
					SCI.error(String.format(SCI.getString("SCI_INVALID_DATE"),
							value));
				} else {
					book.setDate(date);
				}
			} else if ("intro".equals(key)) {
				book.setIntro(value);
			} else {
				book.setAttribute(key, value);
			}
		}
	}

	public static Book openBook(File input, String format, Map<String, Object> kw,
			Map<String, Object> attributes) {
		if (format == null || "".equals(format)) {
			format = FileUtils.getExtension(input.getPath()).toLowerCase();
		}
		Book book = null;
		try {
			book = Jem.readBook(input, format, kw);
			setAttributes(book, attributes);
		} catch (IOException e) {
			LOG.debug("reading "+format.toUpperCase(), e);
		} catch (JemException e) {
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
			LOG.debug("writing "+format.toUpperCase(), e);
		} catch (JemException e) {
			LOG.debug("writing "+format.toUpperCase(), e);
		}
		return path;
	}

	public static String newBook(Map<String, Object> attributes, File output,
			String outFormat, Map<String, Object> outKw) {
		Book book = new Book();
		if (attributes != null) {
			setAttributes(book, attributes);
		}
		String path = saveBook(book, output, outFormat, outKw);
		if (path == null) {
			SCI.error(String.format(SCI.getString("SCI_CREATE_FAILED"),
					book.getTitle()));
		}
		return path;
	}

	public static String convertBook(File input, String inFormat,
			Map<String, Object> inKw, Map<String, Object> attributes,
			File output, String outFormat, Map<String, Object> outKw) {
		Book book = openBook(input, inFormat, inKw, attributes);
		if (book == null) {
			SCI.error(String.format(SCI.getString("SCI_READ_FAILED"),
					input.getPath()));
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
		return null;
	}

	private static List<Integer> parseIndexs(String indexs) {
		List<Integer> results = new java.util.ArrayList<Integer>();
		for (String part: indexs.split("\\.")) {
			try {
				results.add(new Integer(part));
			} catch(NumberFormatException ex) {
				SCI.error(String.format(SCI.getString("SCI_INVALID_INDEXS"), indexs));
				return null;
			}
		}
		return results;
	}

	public static String extractBook(File input, String inFormat,
			Map<String, Object> inKw, Map<String, Object> attributes,
			String indexs, File output, String outFormat, Map<String, Object> outKw) {
		Book book = openBook(input, inFormat, inKw, attributes);
		if (book == null) {
			SCI.error(String.format(SCI.getString("SCI_READ_FAILED"),
					input.getPath()));
			return null;
		}
		return null;
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
						sep, showBrackets, false);
			} else if (key.equals("toc")) {
				viewToc(part, new String[]{"title", "cover"},
						SCI.getString("SCI_TOC_INDENT"), true, true);
			} else if (key.equals("text")) {
				try {
					System.out.println(part.getSource().getText());
				} catch (IOException ex) {
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
				if (! ignoreEmpty || value != null) {
					String str = formatVariant(value);
					if (! "".equals(str)) {
						lines.add(String.format(SCI.getString("SCI_ATTRIBUTE_FORMAT"),
								key, "\"" + str + "\""));
					}
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

	private static void viewExtension(Book book, String[] names, boolean ignoreEmpty) {
		for (String name: names) {
			Object value = book.getItem(name, null);
			if (! ignoreEmpty || value != null) {
				String str = formatVariant(value);
				if (! "".equals(str)) {
					System.out.println(String.format(SCI.getString("SCI_ITEM_FORMAT"),
							name, Jem.getVariantType(value), str));
				}
			}
		}
	}

	public static boolean viewBook(File input, String inFormat, Map<String, Object> inKw,
			Map<String, Object> attributes, String[] keys) {
		Book book = openBook(input, inFormat, inKw, attributes);
		if (book == null) {
			SCI.error(String.format(SCI.getString("SCI_READ_FAILED"),
					input.getPath()));
			return false;
		}
		for (String key: keys) {
			if (key.equals("ext")) {
				viewExtension(book, book.itemNames().toArray(new String[0]), false);
			} else if (key.matches(CHAPTER_REGEX)) {

			} else if (key.matches(ITEM_REGEX)) {
			} else {
				viewPart(book, new String[]{key},
						System.getProperty("line.separator"), false, false);
			}
		}
		return true;
	}
}

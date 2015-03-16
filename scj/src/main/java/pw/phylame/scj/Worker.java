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
import java.util.Map;
import pw.phylame.jem.core.Book;

/**
 * Utility class for SCJ.
 */
public final class Worker {
	public static Book openBook(File input, String format, Map<String, Object> kw) {
		return null;
	}
	
	public static String saveBook(Book book, File output, String format,
			Map<String, Object> kw) {
		return null;
	}
	
	public static void setProperties(Book book, Map<String, Object> attributes) {
		
	}
	
	public static String convertBook(File input, String inFormat,
			Map<String, Object> inKw, Map<String, Object> attributes,
			File output, String outFormat, Map<String, Object> outKw) {
		Book book = openBook(input, inFormat, inKw);
		
		return null;
	}
	
	public static String joinBook(File[] inputs, Map<String, Object> inKw,
			Map<String, Object> attributes, File output, String outFormat,
			Map<String, Object> outKw) {
		return null;
	}
	
	public static String extractBook(File input, String inFormat,
			Map<String, Object> inKw, Map<String, Object> attributes,
			int[] indexs, File output, String outFormat, Map<String, Object> outKw) {
		return null;
	}
	
	public static String[] getProperty(File input, String inFormat,
			Map<String, Object> inKw, Map<String, Object> attributes,
			String[] names) {
		return null;
	}
}

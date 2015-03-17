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
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.CommandLineParser;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import pw.phylame.jem.core.Jem;
import pw.phylame.tools.StringUtils;
import pw.phylame.jem.core.BookHelper;

/**
 * SCI (Simple Console Interface) for Jem.
 */
public final class SCI {
	private static Log LOG = LogFactory.getLog(SCI.class);

	/** SCJ version message */
	public static final String VERSION = "1.0-SNAPSHOT";

	public static String Name = "scj";

	private static ResourceBundle bundle = ResourceBundle.getBundle("i18n.scj",
			Locale.getDefault());

	public static String getString(String key) {
		return bundle.getString(key);
	}

	private static Options makeOptions() {
		Options options = new Options();
		options.addOption("h", "help", false, getString("HELP_DESCRIPTION"));
		options.addOption("v", "version", false, getString("HELP_VERSION"));
		options.addOption("l", "list", false, getString("HELP_LIST"));

		// specified
		Option inFormat = OptionBuilder.withArgName(
				getString("ARG_FORMAT")).hasArg().withDescription(
						getString("HELP_FROM_FORMAT")).create("f");
		Option outFormat = OptionBuilder.withArgName(
				getString("ARG_FORMAT")).hasArg().withDescription(
						String.format(getString("HELP_TO_FORMAT"),
								Jem.PMAB_FORMAT.toUpperCase())
						).create("t");
		Option output = OptionBuilder.withArgName(
				getString("ARG_PATH")).hasArg().withDescription(
						getString("HELP_OUTPUT")).create("o");
		options.addOption(inFormat).addOption(outFormat).addOption(output);

		// operations
		Option create = new Option("n", getString("HELP_NEW"));
		Option convert = new Option("c", getString("HELP_CONVERT"));
		Option join = new Option("j", getString("HELP_JOIN"));
		Option extract = OptionBuilder.withArgName(
				getString("ARG_INDEX")).hasArg().withDescription(
						getString("HELP_EXTRACT")).create("x");
		Option view = OptionBuilder.withArgName(
				getString("ARG_NAME")).hasArg().withValueSeparator().withDescription(
						getString("HELP_VIEW")).create("V");
		options.addOptionGroup(new OptionGroup().addOption(create).addOption(
				convert).addOption(join).addOption(extract).addOption(view));

		Option attr = OptionBuilder.withArgName(
			getString("ARG_KV")).hasArgs(2).withValueSeparator().withDescription(
					getString("HELP_ATTRIBUTE")).create("A");
		Option inKW = OptionBuilder.withArgName(
				getString("ARG_KV")).hasArgs(2).withValueSeparator().withDescription(
						getString("HELP_IN_ARGUMENT")).create("P");
		Option outKw = OptionBuilder.withArgName(
				getString("ARG_KV")).hasArgs(2).withValueSeparator().withDescription(
						getString("HELP_OUT_ARGUMENT")).create("M");

		options.addOption(attr).addOption(inKW).addOption(outKw);

		return options;
	}

	private static Map<String, Object> parseArguments(java.util.Properties prop) {
		Map<String, Object> map = new java.util.HashMap<String, Object>();
		for (String key: prop.stringPropertyNames()) {
			map.put(key, prop.getProperty(key));
		}
		return map;
	}

	private static void showVersion() {
		String os = System.getProperty("os.name");
		String arch = System.getProperty("os.arch");
		System.out.printf("SCI for Jem v%s on %s(%s)\n", VERSION, os, arch);
		System.out.printf("jem-core: %s\n", Jem.VERSION);
		System.out.printf("%s\n", getString("SCJ_COPYRIGHTS"));
	}

	private static void showSupported() {
		System.out.println(getString("LIST_SUPPORTED_TITLE"));
		System.out.printf(" %s %s\n", getString("LIST_INPUT"),
			StringUtils.join(BookHelper.getSupportedParsers(), ",").toUpperCase());
		System.out.printf(" %s %s\n", getString("LIST_OUTPUT"),
			StringUtils.join(BookHelper.getSupportedMakers(), ",").toUpperCase());
	}

	public static void error(String msg) {
		System.err.println(Name + ": " + msg);
	}

	public static void echo(String msg) {
		System.out.println(Name + ": " + msg);
	}

	private static void printError(String name, String syntax, ParseException e) {
		String msg;
		String clazz = e.getClass().getSimpleName();
		if ("UnrecognizedOptionException".equals(clazz)) {
			msg = String.format(getString("SCI_UNRECOGNIZED_OPTION"),
					((org.apache.commons.cli.UnrecognizedOptionException)e).getOption());
		} else if ("MissingOptionException".equals(clazz)) {
			msg = String.format(getString("SCI_MISSING_OPTION"),
					((org.apache.commons.cli.MissingOptionException)e).getMissingOptions());
		} else if ("MissingArgumentException".equals(clazz)) {
			msg = String.format(getString("SCI_MISSING_ARGUMENT"),
					"-" + ((org.apache.commons.cli.MissingArgumentException)e).getOption().getOpt());
		} else if ("AlreadySelectedException".equals(clazz)) {
			org.apache.commons.cli.AlreadySelectedException ex = (org.apache.commons.cli.AlreadySelectedException)e;
			msg = String.format(getString("SCI_MORE_OPTIONS"),
					"-" + ex.getOptionGroup().getSelected() + ", -" + ex.getOption().getOpt());
		} else {
			msg = e.getMessage();
		}
		error(msg);
		System.out.println(syntax);
	}

	// command type
	enum Command {
		New, Convert, Join, Extract, View
	}

	public static int exec(String name, String[] args) {
		Options options = makeOptions();
		final String SCJ_SYNTAX = String.format(getString("SCI_SYNTAX"), name);
		CommandLine cmd;
		CommandLineParser parser = new PosixParser();
		try {
			cmd = parser.parse(options, args);
		} catch (ParseException e) {
			printError(name, SCJ_SYNTAX, e);
			return -1;
		}
		if (cmd.hasOption("h")) {
			HelpFormatter hf = new HelpFormatter();
			hf.setSyntaxPrefix("");
			hf.printHelp(SCJ_SYNTAX, getString("SCI_OPTIONS"), options,
					getString("SCJ_BUG_REPORT"));
			return 0;
		} else if (cmd.hasOption("v")) {
			showVersion();
			return 0;
		} else if (cmd.hasOption("l")) {
			showSupported();
			return 0;
		}
		String[] viewNames = {"all"};
		String indexs = null;
		Command command = Command.View;
		if (cmd.hasOption("c")) {
			command = Command.Convert;
		} else if (cmd.hasOption("V")) {
			viewNames = cmd.getOptionValues("V");
		} else if (cmd.hasOption("n")) {
			command = Command.New;
		} else if (cmd.hasOption("j")) {
			command = Command.Join;
		} else if (cmd.hasOption("x")) {
			command = Command.Extract;
			indexs = cmd.getOptionValue("x");
		}
		String inFormat = cmd.getOptionValue("f"), outFormat = cmd.getOptionValue("t");
		outFormat = outFormat == null ? Jem.PMAB_FORMAT : outFormat;
		if (inFormat != null && ! BookHelper.getSupportedParsers().contains(inFormat)) {
			error(String.format(getString("SCI_IN_UNSUPPORTED"), inFormat));
			System.out.println(getString("SCI_UNSUPPORTED_HELP"));
			return -1;
		}
		if (! BookHelper.getSupportedMakers().contains(outFormat)) {
			error(String.format(getString("SCI_OUT_UNSUPPORTED"), outFormat));
			System.out.println(getString("SCI_UNSUPPORTED_HELP"));
			return -1;
		}
		// inputs
		String[] files = cmd.getArgs();
		if (files.length == 0 && command != Command.New) {
			error(getString("SCI_NO_INPUT"));
			return -1;
		}
		// output
		String out = cmd.getOptionValue("o");
		File output = new File(out == null ? "." : out);

		Map<String, Object> inKw = parseArguments(cmd.getOptionProperties("P"));
		Map<String, Object> outKw = parseArguments(cmd.getOptionProperties("M"));
		Map<String, Object> attrs = parseArguments(cmd.getOptionProperties("A"));

		// create new book
		if (command == Command.New) {
			String result = Worker.newBook(attrs, output, outFormat, outKw);
			if (result != null) {
				System.out.println(result);
				return 0;
			} else {
				return -1;
			}
		}

		List<File> inputs = new java.util.ArrayList<File>();
		// exit status
		int status = 0;
		for (String file: files) {
			File input = new File(file);
			if (! input.exists()) {
				error(String.format(getString("SCI_NOT_EXISTS"), input.getPath()));
				status = -1;
				continue;
			}
			String result = null;
			switch (command) {
			case View:
				if (! Worker.viewBook(input, inFormat, inKw, attrs, viewNames)) {
					status = -1;
				}
				break;
			case Convert:
				result = Worker.convertBook(input, inFormat, inKw, attrs, output,
						outFormat, outKw);
				status = result != null ? 0 : 1;
				break;
			case Join:
				inputs.add(input);
				break;
			case Extract:
				result = Worker.extractBook(input, inFormat, inKw, attrs, indexs,
						output, outFormat, outKw);
				status = result != null ? 0 : 1;
				break;
			}
			if (result != null) {
				System.out.println(result);
			}
		}
		// join books
		if (command == Command.Join) {
			String result = Worker.joinBook(inputs.toArray(new File[0]), inKw,
					attrs, output, outFormat, outKw);
			if (result != null) {
				System.out.println(result);
				return 0;
			} else {
				return -1;
			}
		}
		return status;
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		System.exit(exec(Name, args));
	}
}

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
import java.text.MessageFormat;
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

import pw.phylame.jem.core.Jem;
import pw.phylame.tools.StringUtils;
import pw.phylame.tools.file.FileUtils;
import pw.phylame.jem.core.BookHelper;

/**
 * SCI (Simple Console Interface) for Jem.
 */
public final class SCI {

	/** SCJ version message */
	public static final String VERSION = "1.0.3-SNAPSHOT";

	public static String Name = "scj";

	private static ResourceBundle bundle = null;

	static {
		String locale = System.getProperty("scj.locale");
		if (locale == null) {
			bundle = ResourceBundle.getBundle("i18n.scj", Locale.getDefault());
		} else {
			bundle = ResourceBundle.getBundle("i18n.scj", Locale.forLanguageTag(locale));
		}
	}

	public static String getText(String key, Object... objects) {
		String pattern = bundle.getString(key);
		return MessageFormat.format(pattern, objects);
	}

	/**
	 * Makes CLI options.
	 * @return the options
	 */
	private static Options makeOptions() {
		Options options = new Options();
		options.addOption("h", "help", false, getText("HELP_DESCRIPTION"));
		options.addOption("v", "version",false, getText("HELP_VERSION"));
		options.addOption("l", "list", false, getText("HELP_LIST"));

		// specified
		Option inFormat = OptionBuilder.withArgName(
				getText("ARG_FORMAT")).hasArg().withDescription(
						getText("HELP_FROM_FORMAT")).create("f");

		Option outFormat = OptionBuilder.withArgName(
				getText("ARG_FORMAT")).hasArg().withDescription(
						getText("HELP_TO_FORMAT", Jem.PMAB_FORMAT.toUpperCase())).create("t");

		Option output = OptionBuilder.withArgName(
				getText("ARG_PATH")).hasArg().withDescription(
						getText("HELP_OUTPUT")).create("o");

		options.addOption(inFormat).addOption(outFormat).addOption(output);

		// operations
		Option convert = new Option("c", "convert", false, getText("HELP_CONVERT"));
		Option join = new Option("j", "join", false, getText("HELP_JOIN"));
		Option extract = OptionBuilder.withArgName(
				getText("ARG_INDEX")).hasArg().withDescription(
						getText("HELP_EXTRACT")).create("x");

		Option view = OptionBuilder.withArgName(
				getText("ARG_NAME")).hasArg().withValueSeparator().withDescription(
						getText("HELP_VIEW")).create("w");

		options.addOptionGroup(new OptionGroup().addOption(
				convert).addOption(join).addOption(extract).addOption(view));

		Option attr = OptionBuilder.withArgName(
				getText("ARG_KV")).hasArgs(2).withValueSeparator().withDescription(
						getText("HELP_ATTRIBUTE")).create("a");

		Option inKW = OptionBuilder.withArgName(
				getText("ARG_KV")).hasArgs(2).withValueSeparator().withDescription(
						getText("HELP_IN_ARGUMENT")).create("p");

		Option outKw = OptionBuilder.withArgName(
				getText("ARG_KV")).hasArgs(2).withValueSeparator().withDescription(
						getText("HELP_OUT_ARGUMENT")).create("m");

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
		System.out.printf("SCI for Jem v%s on %s (%s)\n", VERSION, System.getProperty("os.name"),
				System.getProperty("os.arch"));
		System.out.printf("jem-core: %s\n", Jem.VERSION);
		System.out.printf("%s\n", getText("SCJ_COPYRIGHTS"));
	}

	private static void showSupported() {
		System.out.println(getText("LIST_SUPPORTED_TITLE"));
		System.out.printf(" %s %s\n", getText("LIST_INPUT"),
				StringUtils.join(BookHelper.supportedParsers(), " ").toUpperCase());
		System.out.printf(" %s %s\n", getText("LIST_OUTPUT"),
				StringUtils.join(BookHelper.supportedMakers(), " ").toUpperCase());
	}

	public static void error(String msg) {
		System.err.println(Name + ": " + msg);
	}

	public static void echo(String msg) {
		System.out.println(Name + ": " + msg);
	}

	/**
	 * Prints CLI errors.
	 * @param name SCI name
	 * @param syntax SCI syntax
	 * @param e the exception
	 */
	private static void cliError(String name, String syntax, ParseException e) {
		String msg;
		String clazz = e.getClass().getSimpleName();
		if ("UnrecognizedOptionException".equals(clazz)) {
			msg = getText("SCI_UNRECOGNIZED_OPTION",
					((org.apache.commons.cli.UnrecognizedOptionException)e).getOption());
		} else if ("MissingOptionException".equals(clazz)) {
			msg = getText("SCI_MISSING_OPTION",
					((org.apache.commons.cli.MissingOptionException)e).getMissingOptions());
		} else if ("MissingArgumentException".equals(clazz)) {
			msg = getText("SCI_MISSING_ARGUMENT",
					"-" + ((org.apache.commons.cli.MissingArgumentException)e).getOption().getOpt());
		} else if ("AlreadySelectedException".equals(clazz)) {
			org.apache.commons.cli.AlreadySelectedException ex = (org.apache.commons.cli.AlreadySelectedException)e;
			msg = getText("SCI_MORE_OPTIONS",
					"-" + ex.getOptionGroup().getSelected() + ", -" + ex.getOption().getOpt());
		} else {
			msg = e.getMessage();
		}
		error(msg);
		System.out.println(syntax);
	}

	// command type
	enum Command {
		Convert, Join, Extract, View
	}

	public static int exec(String name, String[] args) {
		final String SCJ_SYNTAX = getText("SCI_SYNTAX", name);
		Options options = makeOptions();
		CommandLine cmd;
		try {
			cmd = new PosixParser().parse(options, args);    // POSIX style
		} catch (ParseException e) {
			cliError(name, SCJ_SYNTAX, e);
			return -1;
		}
		if (cmd.hasOption("h")) {
			HelpFormatter hf = new HelpFormatter();
			hf.setSyntaxPrefix("");
			hf.printHelp(80, SCJ_SYNTAX, getText("SCI_OPTIONS_PREFIX"), options, getText("SCJ_BUG_REPORT"));
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
		} else if (cmd.hasOption("w")) {
			viewNames = cmd.getOptionValues("w");
		} else if (cmd.hasOption("j")) {
			command = Command.Join;
		} else if (cmd.hasOption("x")) {
			command = Command.Extract;
			indexs = cmd.getOptionValue("x");
		}
		String inFormat = cmd.getOptionValue("f"), outFormat = cmd.getOptionValue("t");
		outFormat = outFormat == null ? Jem.PMAB_FORMAT : outFormat;
		if (! BookHelper.supportedMakers().contains(outFormat)) {
			error(getText("SCI_OUT_UNSUPPORTED", outFormat));
			System.out.println(getText("SCI_UNSUPPORTED_HELP"));
			return -1;
		}
		// inputs
		String[] files = cmd.getArgs();
		if (files.length == 0) {
			error(getText("SCI_NO_INPUT"));
			return -1;
		}
		// output
		String out = cmd.getOptionValue("o");
		File output = new File(out == null ? "." : out);    // if not specified use current directory

		Map<String, Object> inKw = parseArguments(cmd.getOptionProperties("p"));
		Map<String, Object> outKw = parseArguments(cmd.getOptionProperties("m"));
		Map<String, Object> attrs = parseArguments(cmd.getOptionProperties("a"));

		List<File> inputs = new java.util.ArrayList<File>();
		// exit status
		int status = 0;
		for (String file: files) {
			File input = new File(file);
			if (! input.exists()) {
				error(getText("SCI_NOT_EXISTS", input.getPath()));
				status = -1;
				continue;
			}
			String inFmt = inFormat;
			if (inFmt == null) {
				inFmt = FileUtils.getExtension(file);
			}
			if (inFmt != null && ! BookHelper.supportedParsers().contains(inFmt)) {
				error(getText("SCI_IN_UNSUPPORTED", inFmt));
				System.out.println(getText("SCI_UNSUPPORTED_HELP"));
				status = -1;
				continue;
			}
			String result = null;
			switch (command) {
			case View:
				if (! Worker.viewBook(input, inFmt, inKw, attrs, viewNames)) {
					status = -1;
				}
				break;
			case Convert:
				result = Worker.convertBook(input, inFmt, inKw, attrs, output, outFormat, outKw);
				status = Math.min(result != null ? 0 : 1, status);
				break;
			case Join:
				inputs.add(input);
				break;
			case Extract:
				result = Worker.extractBook(input, inFmt, inKw, attrs, indexs, output, outFormat, outKw);
				status = Math.min(result != null ? 0 : 1, status);
				break;
			}
			if (result != null) {
				System.out.println(result);
			}
		}
		// join books
		if (command == Command.Join) {
			String result = Worker.joinBook(inputs.toArray(new File[0]), inKw, attrs, output,
					outFormat, outKw);
			if (result != null) {
				System.out.println(result);
			} else {
				status = 1;
			}
		}
		return status;
	}

	public static void main(String[] args) {
		System.exit(exec(Name, args));
	}
}

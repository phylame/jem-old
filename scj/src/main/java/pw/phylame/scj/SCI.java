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
import java.util.Locale;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Properties;

import pw.phylame.pat.gaf.Translator;
import pw.phylame.pat.gaf.Application;
import pw.phylame.jem.core.Jem;
import pw.phylame.jem.core.BookHelper;
import pw.phylame.tools.StringUtils;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.FilenameUtils;

/**
 * SCI (Simple Console Interface) for Jem.
 */
public final class SCI extends Application implements Constants {

    private AppConfig config;

    SCI(String[] args) {
        super(NAME, VERSION, args);

        ensureHomeExisted();

        config = new AppConfig();

        Locale.setDefault(config.getAppLocale());

        Translator translator = new Translator(I18N_PATH);
        installTranslator(translator);
    }

    static SCI getInstance() {
        return (SCI) getApplication();
    }

    AppConfig getConfig() {
        return config;
    }

    /**
     * Makes CLI options.
     * @return the options
     */
    private Options makeOptions() {
        Options options = new Options();
        options.addOption("h", "help", false, getText("HELP_DESCRIPTION"));
        options.addOption("v", "version", false, getText("HELP_VERSION"));
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
        options.addOption("c", "convert", false, getText("HELP_CONVERT"));
        options.addOption("j", "join", false, getText("HELP_JOIN"));

        Option extract = OptionBuilder.withArgName(
                getText("ARG_INDEX")).hasArg().withDescription(
                        getText("HELP_EXTRACT")).create("x");

        options.addOption(extract);

        Option view = OptionBuilder.withArgName(
                getText("ARG_NAME")).hasArg().withValueSeparator().withDescription(
                        getText("HELP_VIEW")).create("w");

        options.addOption(view);

        Option attr = OptionBuilder.withArgName(
                getText("ARG_KV")).hasArgs(2).withValueSeparator().withDescription(
                        getText("HELP_ATTRIBUTE")).create("a");

        Option item = OptionBuilder.withArgName(
                getText("ARG_KV")).hasArgs(2).withValueSeparator().withDescription(
                getText("HELP_ITEM")).create("i");

        Option inKW = OptionBuilder.withArgName(
                getText("ARG_KV")).hasArgs(2).withValueSeparator().withDescription(
                        getText("HELP_IN_ARGUMENT")).create("p");

        Option outKw = OptionBuilder.withArgName(
                getText("ARG_KV")).hasArgs(2).withValueSeparator().withDescription(
                        getText("HELP_OUT_ARGUMENT")).create("m");

        options.addOption(attr).addOption(item).addOption(inKW).addOption(outKw);

        return options;
    }

    private HashMap<String, Object> parseArguments(Properties prop) {
        HashMap<String, Object> map = new HashMap<>();
        for (String key: prop.stringPropertyNames()) {
            map.put(key, prop.getProperty(key));
        }
        return map;
    }

    private void showVersion() {
        System.out.printf("SCI for Jem v%s on %s (%s)\n", Constants.VERSION,
                System.getProperty("os.name"),
                StringUtils.toCapital(System.getProperty("os.arch")));
        System.out.printf("Jem Core: %s by %s\n", Jem.VERSION, Jem.VENDOR);
        System.out.printf("Jem Formats: %s by %s\n",
                pw.phylame.jem.formats.util.Version.VERSION,
                pw.phylame.jem.formats.util.Version.VENDOR);
        System.out.printf("%s\n", getText("SCJ_COPYRIGHTS"));
    }

    private void showSupported() {
        System.out.println(getText("LIST_SUPPORTED_TITLE"));
        System.out.printf(" %s %s\n", getText("LIST_INPUT"),
                StringUtils.join(BookHelper.supportedParsers(), " ").toUpperCase());
        System.out.printf(" %s %s\n", getText("LIST_OUTPUT"),
                StringUtils.join(BookHelper.supportedMakers(), " ").toUpperCase());
    }

    void error(String msg) {
        System.err.println(getName() + ": " + msg);
    }

    void echo(String msg) {
        System.out.println(getName() + ": " + msg);
    }

    /**
     * Prints CLI syntax errors to stderr.
     * @param syntax SCI syntax
     * @param e the exception
     */
    private void cliError(String syntax, ParseException e) {
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
        } else {
            msg = e.getMessage();
        }
        error(msg);
        System.out.println(syntax);
    }

    @Override
    public void run() {
        System.exit(exec());
    }

    // command type
    enum Command {
        Convert, Join, Extract, View
    }

    int exec() {
        final String SCJ_SYNTAX = getText("SCI_SYNTAX", getName());
        Options options = makeOptions();
        CommandLine cmd;
        try {
            cmd = new PosixParser().parse(options, getArguments());    // POSIX style
        } catch (ParseException e) {
            cliError(SCJ_SYNTAX, e);
            return -1;
        }
        if (cmd.hasOption("h")) {
            HelpFormatter hf = new HelpFormatter();
            hf.setSyntaxPrefix("");
            hf.printHelp(80, SCJ_SYNTAX, getText("SCI_OPTIONS_PREFIX"),
                    options, getText("SCJ_BUG_REPORT"));
            return 0;
        } else if (cmd.hasOption("v")) {
            showVersion();
            return 0;
        } else if (cmd.hasOption("l")) {
            showSupported();
            return 0;
        }

        String[] viewNames = {"all"};
        String indexes = null;
        Command command = Command.View;
        if (cmd.hasOption("c")) {
            command = Command.Convert;
        } else if (cmd.hasOption("w")) {
            viewNames = cmd.getOptionValues("w");
        } else if (cmd.hasOption("j")) {
            command = Command.Join;
        } else if (cmd.hasOption("x")) {
            command = Command.Extract;
            indexes = cmd.getOptionValue("x");
        }

        // input and output formats
        String inFormat = cmd.getOptionValue("f"), outFormat = cmd.getOptionValue("t");
        outFormat = outFormat == null ? config.getDefaultFormat() : outFormat;

        // input files
        String[] files = cmd.getArgs();
        if (files.length == 0) {
            error(getText("SCI_NO_INPUT"));
            return -1;
        }

        // output
        String out = cmd.getOptionValue("o");

        // if not specified use current directory
        File output = new File(out == null ? "." : out);

        if (!BookHelper.hasMaker(outFormat)) {
            error(getText("SCI_OUT_UNSUPPORTED", outFormat));
            System.out.println(getText("SCI_UNSUPPORTED_HELP"));
            return -1;
        }

        HashMap<String, Object> inKw = parseArguments(cmd.getOptionProperties("p"));
        HashMap<String, Object> outKw = parseArguments(cmd.getOptionProperties("m"));
        HashMap<String, Object> attrs = parseArguments(cmd.getOptionProperties("a"));
        HashMap<String, Object> items = parseArguments(cmd.getOptionProperties("i"));

        ArrayList<String> inputs = new ArrayList<>();
        // exit status
        int status = 0;
        for (String file: files) {
            String inFmt = inFormat;
            if (inFmt == null) {
                inFmt = FilenameUtils.getExtension(file);
            }
            if (! BookHelper.hasParser(inFmt)) {
                error(getText("SCI_IN_UNSUPPORTED", inFmt));
                System.out.println(getText("SCI_UNSUPPORTED_HELP"));
                status = -1;
                continue;
            }
            String result = null;
            switch (command) {
            case View:
                if (! Worker.viewBook(file, inFmt, inKw, attrs, items, viewNames)) {
                    status = -1;
                }
                break;
            case Convert:
                result = Worker.convertBook(
                        file, inFmt, inKw, attrs, items, output, outFormat, outKw);
                status = Math.min(result != null ? 0 : 1, status);
                break;
            case Join:
                // process this task later
                inputs.add(file);
                break;
            case Extract:
                result = Worker.extractBook(
                        file, inFmt, inKw, attrs, items, indexes, output, outFormat, outKw);
                status = Math.min(result != null ? 0 : 1, status);
                break;
            }
            if (result != null) {
                System.out.println(result);
            }
        }
        // join books
        if (command == Command.Join) {
            String result = Worker.joinBook(
                    inputs, inKw, attrs, items, output, outFormat, outKw);
            if (result != null) {
                System.out.println(result);
            } else {
                status = 1;
            }
        }
        return status;
    }

    public static void main(String[] args) {
        SCI app = new SCI(args);
        app.start();
    }
}

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

import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.Properties;

import pw.phylame.gaf.cli.*;
import pw.phylame.gaf.core.Translator;
import pw.phylame.jem.core.Jem;
import pw.phylame.jem.core.BookHelper;
import org.apache.commons.cli.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import pw.phylame.jem.formats.ucnovel.NovelConfig;
import pw.phylame.jem.formats.ucnovel.NovelDbReader;
import pw.phylame.jem.formats.ucnovel.NovelInfo;
import pw.phylame.jem.formats.ucnovel.UCNovelParser;
import pw.phylame.jem.formats.util.ParserException;

public class SCI extends CApplication implements Constants {
    private static final Log LOG = LogFactory.getLog(SCI.class);

    SCI(String[] args) {
        super(NAME, VERSION, args);
        errorTipKey = "error.detailTip";
    }

    static SCI sharedInstance() {
        return (SCI) instance;
    }

    @Override
    protected void makeOptions() {
        syntax = getText("sci.syntax", getName());

        // show help
        addOption(new Option(OPTION_HELP, false, getText("help.description")),
                new ShowHelp());

        // show version
        addOption(new Option(OPTION_VERSION, false, getText("help.version")),
                new ShowVersion());

        // list supported
        addOption(new Option(OPTION_LIST, false, getText("help.list")),
                new ListSupported());

        // verbose
        Option option = Option.builder(OPTION_DEBUG_LEVEL)
                .argName(getText("help.debugLevel.argName"))
                .hasArg()
                .desc(getText("help.debugLevel", debugLevel))
                .build();
        addOption(option, new GetDebugLevel());

        // input format
        option = Option.builder(OPTION_INPUT_FORMAT)
                .argName(getText("help.formatName"))
                .hasArg()
                .desc(getText("help.inputFmt"))
                .build();
        addOption(option, new GetInputFormat());

        // parse arguments
        option = Option.builder(OPTION_PARSE_ARGUMENTS)
                .argName(getText("help.kvName"))
                .numberOfArgs(2)
                .valueSeparator()
                .desc(getText("help.parserArgs"))
                .build();
        addOption(option, new CFetchProperties(OPTION_PARSE_ARGUMENTS));

        // output attributes
        option = Option.builder(OPTION_ATTRIBUTES)
                .argName(getText("help.kvName"))
                .numberOfArgs(2)
                .valueSeparator()
                .desc(getText("help.attribute"))
                .build();
        addOption(option, new CFetchProperties(OPTION_ATTRIBUTES));

        // output extension
        option = Option.builder(OPTION_EXTENSIONS)
                .argName(getText("help.kvName"))
                .numberOfArgs(2)
                .valueSeparator()
                .desc(getText("help.extension"))
                .build();
        addOption(option, new CFetchProperties(OPTION_EXTENSIONS));

        // output path
        option = Option.builder(OPTION_OUTPUT)
                .argName(getText("help.output.argName"))
                .hasArg()
                .desc(getText("help.output.path"))
                .build();
        addOption(option, new CFetchString(OPTION_OUTPUT));

        // output format
        option = Option.builder(OPTION_OUTPUT_FORMAT)
                .argName(getText("help.formatName"))
                .hasArg()
                .desc(getText("help.outputFmt", Jem.PMAB))
                .build();
        addOption(option, new GetOutputFormat());

        // make arguments
        option = Option.builder(OPTION_MAKE_ARGUMENTS)
                .argName(getText("help.kvName"))
                .numberOfArgs(2)
                .valueSeparator()
                .desc(getText("help.makerArgs"))
                .build();
        addOption(option, new CFetchProperties(OPTION_MAKE_ARGUMENTS));

        OptionGroup optionGroup = new OptionGroup();

        // convert
        option = new Option(OPTION_CONVERT, false, getText("help.convert"));
        optionGroup.addOption(option);
        addOption(option, new ConvertBook());

        // join
        option = new Option(OPTION_JOIN, false, getText("help.join"));
        optionGroup.addOption(option);
        addOption(option, new JoinBook());

        // extract and indices
        option = Option.builder(OPTION_EXTRACT)
                .argName(getText("help.extract.argName"))
                .hasArg()
                .desc(getText("help.extract"))
                .build();
        optionGroup.addOption(option);
        addOption(option, new ExtractBook(OPTION_EXTRACT));

        // view and names
        option = Option.builder(OPTION_VIEW)
                .argName(getText("help.view.argName"))
                .hasArg()
                .valueSeparator()
                .desc(getText("help.view"))
                .build();
        defaultCommand = new ViewBook(OPTION_VIEW);
        addOption(option, defaultCommand);

        addOptionGroup(optionGroup);

        // list uc novels
        option = Option.builder(OPTION_LIST_NOVELS)
                .longOpt(OPTION_LIST_NOVELS_LONG)
                .desc(getText("help.ucnovels"))
                .build();
        addOption(option, new ListUCNovels());
    }

    @Override
    protected void onStart() {
        ensureHomeExisted();
        AppConfig config = AppConfig.sharedInstance();
        Locale.setDefault(config.getAppLocale());
        installTranslator(new Translator(I18N_NAME));
        if (!checkDebugLevel(config.getDebugLevel())) {
            exit(-1);
        }
        super.onStart();
        try {
            loadPlugins();
        } catch (Exception ex) {
            LOG.debug("cannot load plugins", ex);
        }
    }

    private class ShowHelp implements CCommand {
        @Override
        public int perform(CApplication app) {
            HelpFormatter hf = new HelpFormatter();
            hf.setSyntaxPrefix("");
            hf.printHelp(80, syntax, getText("help.prefix"), options,
                    getText("help.feedback"));
            return 0;
        }
    }

    private class ShowVersion implements CCommand {
        @Override
        public int perform(CApplication app) {
            System.out.printf("SCI for Jem v%s on %s (%s)\n", VERSION,
                    System.getProperty("os.name"), System.getProperty("os.arch"));
            System.out.printf(" Jem Core   : %s by %s\n", Jem.VERSION, Jem.VENDOR);
            System.out.printf(" Jem Formats: %s by %s\n",
                    pw.phylame.jem.formats.util.Versions.VERSION,
                    pw.phylame.jem.formats.util.Versions.VENDOR);
            System.out.printf("%s\n", getText("app.copyrights"));
            return 0;
        }
    }

    private class ListSupported implements CCommand {
        @Override
        public int perform(CApplication app) {
            System.out.println(getText("list.title"));
            System.out.printf(" %s %s\n", getText("list.input"),
                    String.join(" ", BookHelper.supportedParsers()).toUpperCase());
            System.out.printf(" %s %s\n", getText("list.output"),
                    String.join(" ", BookHelper.supportedMakers()).toUpperCase());
            return 0;
        }
    }

    private boolean checkDebugLevel(String level) {
        boolean valid = true;
        switch (level) {
            case DEBUG_ECHO:
                debugLevel = DebugLevel.Echo;
                break;
            case DEBUG_TRACE:
                debugLevel = DebugLevel.Trace;
                break;
            case DEBUG_NONE:
                debugLevel = DebugLevel.None;
                break;
            default:
                localizedError("error.invalidDebugLevel", level);
                valid = false;
                break;
        }
        return valid;
    }

    boolean checkInputFormat(String format) {
        if (!BookHelper.hasParser(format)) {
            localizedError("error.input.unsupported", format);
            System.out.println(getText("tip.unsupportedFormat"));
            return false;
        }
        return true;
    }

    boolean checkOutputFormat(String format) {
        if (!BookHelper.hasMaker(format)) {
            localizedError("error.output.unsupported", format);
            System.out.println(getText("tip.unsupportedFormat"));
            return false;
        }
        return true;
    }

    private Worker.InputOption inputOption = null;

    Worker.InputOption getInputOption() {
        if (inputOption == null) {
            inputOption = new Worker.InputOption(getContext());
        }
        return inputOption;
    }

    private Worker.OutputOption outputOption = null;

    Worker.OutputOption getOutputOption() {
        if (outputOption == null) {
            outputOption = new Worker.OutputOption(getContext());
        }
        return outputOption;
    }

    private interface SCITask {
        boolean execute(Worker.InputOption inputOption);
    }

    private int processInputs(CApplication app, SCITask task) {
        String[] inputs = app.getInputs();
        if (inputs.length == 0) {
            localizedError("error.input.empty");
            return -1;
        }
        Worker.InputOption inputOption = getInputOption();
        String initFormat = inputOption.format;
        int status = 0;
        for (String input : inputs) {
            File file = new File(input);

            // check it exists
            if (!file.exists()) {
                localizedError("error.input.notExists", input);
                status = -1;
                continue;
            }

            String format = (initFormat != null) ? initFormat : Jem.formatByExtension(input);
            if (!checkInputFormat(format)) {
                status = -1;
                continue;
            }
            inputOption.file = file;
            inputOption.format = format;
            status = Math.min(status, task.execute(inputOption) ? 0 : -1);
        }
        inputOption.format = initFormat;
        return status;
    }

    private class GetDebugLevel extends CFetchString {
        private GetDebugLevel() {
            super(OPTION_DEBUG_LEVEL);
        }

        @Override
        protected boolean validateValue(String value) {
            return checkDebugLevel(value);
        }
    }

    private class GetInputFormat extends CFetchString {
        private GetInputFormat() {
            super(OPTION_INPUT_FORMAT);
        }

        @Override
        protected boolean validateValue(String value) {
            return checkInputFormat(value);
        }
    }

    private class GetOutputFormat extends CFetchString {
        private GetOutputFormat() {
            super(OPTION_OUTPUT_FORMAT);
        }

        @Override
        protected boolean validateValue(String value) {
            return checkOutputFormat(value);
        }
    }

    private class ListUCNovels implements CCommand {

        @Override
        public int perform(CApplication app) {
            String[] inputs = app.getInputs();
            if (inputs.length == 0) {
                localizedError("error.input.empty");
                return -1;
            }
            String readerPath = null;
            Properties prop = (Properties) app.getContext().get(OPTION_PARSE_ARGUMENTS);
            if (prop != null) {
                readerPath = prop.getProperty(NovelConfig.READER_CONFIG);
            }
            try (NovelDbReader reader = readerPath != null
                    ? UCNovelParser.loadDbReader(readerPath)
                    : UCNovelParser.loadDbReader()) {
                File file;
                for (String input : inputs) {
                    file = new File(input);
                    if (file.isDirectory()) {
                        file = new File(file, UCNovelParser.CATALOG_FILE_NAME);
                    }
                    reader.init(file.getPath());
                    NovelInfo info;
                    for (String novelId : reader.fetchNovels()) {
                        info = reader.fetchInfo(novelId);
                        System.out.println(app.getText("ucnovels.novelTemplate", novelId,
                                info.name, info.author, info.expireTime, info.updateTime, info.table));
                        System.out.println();
                    }
                }
            } catch (IOException | ParserException e) {
                app.localizedError(e, "ucnovels.error");
            }
            return 0;
        }
    }

    private class ConvertBook implements CCommand, SCITask {
        @Override
        public int perform(CApplication app) {
            return processInputs(app, this);
        }

        @Override
        public boolean execute(Worker.InputOption inputOption) {
            return Worker.convertBook(inputOption, getOutputOption());
        }
    }

    private class JoinBook implements CCommand {
        @Override
        public int perform(CApplication app) {
            String[] inputs = app.getInputs();
            if (inputs.length == 0) {
                localizedError("error.input.empty");
                return -1;
            }
            return Worker.joinBook(inputs, getInputOption(), getOutputOption()) ? 0 : -1;
        }
    }

    private class ExtractBook extends CFetchList implements CCommand, SCITask {
        private String[] indices;

        private ExtractBook(String option) {
            super(option);
        }

        @Override
        public int perform(CApplication app) {
            indices = (String[]) app.getContext().get(OPTION_EXTRACT);
            return processInputs(app, this);
        }

        @Override
        public boolean execute(Worker.InputOption inputOption) {
            boolean state = true;
            for (String index : indices) {
                state = Worker.extractBook(inputOption, index, getOutputOption()) && state;
            }
            return state;
        }
    }

    private class ViewBook extends CFetchList implements CCommand, SCITask {
        private String[] names;

        private ViewBook(String option) {
            super(option);
        }

        @Override
        public int perform(CApplication app) {
            names = (String[]) app.getContext().getOrDefault(OPTION_VIEW,
                    new String[]{AppConfig.sharedInstance().getViewKey()});
            return processInputs(app, this);
        }

        @Override
        public boolean execute(Worker.InputOption inputOption) {
            return Worker.viewBook(inputOption, names);
        }
    }

    public static void main(String[] args) {
        new SCI(args).start();
    }
}

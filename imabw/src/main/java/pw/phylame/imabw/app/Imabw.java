/*
 * Copyright 2014-2015 Peng Wan <phylame@163.com>
 *
 * This file is part of Imabw.
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

package pw.phylame.imabw.app;

import java.util.Arrays;
import java.util.Locale;
import java.util.HashMap;
import java.util.LinkedList;

import pw.phylame.gaf.core.TranslateHelper;
import pw.phylame.gaf.core.Translator;
import pw.phylame.gaf.ixin.IResource;
import pw.phylame.gaf.ixin.IApplication;
import pw.phylame.gaf.ixin.IxinUtilities;
import pw.phylame.imabw.app.ui.Viewer;
import pw.phylame.imabw.app.config.UIConfig;
import pw.phylame.imabw.app.config.AppConfig;

/**
 * Imabw application model.
 */
public class Imabw extends IApplication<Viewer> implements Constants {
    private MessageCenter messageCenter;
    private Manager manager;
    CLIContext context;

    protected Imabw(String[] args) {
        super(NAME, VERSION, args);

        // firstly, parse CLI arguments
        parseCLIOptions();
    }

    public static Imabw sharedInstance() {
        return (Imabw) instance;
    }

    /**
     * Holds data from CLI arguments.
     */
    class CLIContext {
        // input book files
        LinkedList<String> inputs = new LinkedList<>();

        // input book format
        String format = null;

        // Jem parser argument
        HashMap<String, Object> kw = new HashMap<>();
    }

    /**
     * Prints CLI usage and exit Imabw.
     *
     * @param status exit status code
     */
    private void printCLIUsage(int status) {
        System.out.println("usage: imabw: -p <key=value> -f <format> files");
        System.exit(status);
    }

    private void parseCLIOptions() {
        context = new CLIContext();

        String[] argv = getArguments();
        int i = 0, length = argv.length;
        while (i < length) {
            String arg = argv[i++];
            switch (arg) {
                case "-p": {
                    if (i < length) {               // has value
                        String[] parts = argv[i++].split("=", 2);
                        if (parts.length != 2) {
                            error("-p require <key=value> argument");
                            printCLIUsage(-1);
                        } else {
                            context.kw.put(parts[0], parts[1]);
                        }
                    } else {
                        error("-p require <key=value> argument");
                        printCLIUsage(-1);
                    }
                }
                break;
                case "-f": {
                    if (i < length) {               // has value
                        context.format = argv[i++];
                    } else {
                        error("-f require format");
                        printCLIUsage(-1);
                    }
                }
                break;
                case "-h":
                    printCLIUsage(0);
                    break;
                default: {
                    --i;
                    context.inputs.add(argv[i++]);
                }
                break;
            }
        }
    }

    @Override
    protected void onStart() {
        ensureHomeExisted();

        AppConfig config = AppConfig.sharedInstance();
        Locale.setDefault(config.getAppLocale());

        debugLevel = DebugLevel.Echo;

        String l10n = System.getProperty("imabw.debug.l10n", null);
        if (l10n != null) {
            String parts[] = l10n.split(";"), tags[], path;
            if (parts.length == 1) {
                path = "src/main/resources/" + I18N_NAME;
                tags = new String[]{parts[0]};
            } else {
                path = parts[parts.length - 1];
                tags = Arrays.copyOfRange(parts, 0, parts.length - 1);
            }
            installTranslator(new TranslateHelper(I18N_NAME, path, tags));
        } else {
            installTranslator(new Translator(I18N_NAME));
        }

        setDelegate(manager = new Manager(this));
    }

    @Override
    protected Viewer createForm() {
        UIConfig config = UIConfig.sharedInstance();

        IxinUtilities.useMnemonic = config.isMnemonicEnable();
        installResource(new IResource(RESOURCE_DIR, IMAGE_DIR + config.getIconSet()));

        IxinUtilities.setAntiAliasing(config.isAntiAliasing());
        if (config.isWindowDecorated()) {
            IxinUtilities.setWindowDecorated(true);
        }
        IxinUtilities.setLafTheme(config.getLafTheme());
        IxinUtilities.setGlobalFont(config.getGlobalFont());
        return new Viewer();
    }

    @Override
    public void run() {
        super.run();
        messageCenter = new MessageCenter(getForm());
        manager.start();
    }

    public void message(String message) {
        messageCenter.message(message);
    }

    public void localizedMessage(String key, Object... args) {
        messageCenter.message(getText(key, args));
    }

    public Manager getManager() {
        return manager;
    }

    public static void main(String[] args) {
        new Imabw(args).start();
    }
}

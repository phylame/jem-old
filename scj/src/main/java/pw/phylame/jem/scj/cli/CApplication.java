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

package pw.phylame.jem.scj.cli;

import java.util.Map;
import java.util.HashMap;

import org.apache.commons.cli.*;

import pw.phylame.gaf.core.Application;

public abstract class CApplication extends Application {
    public CApplication(String name, String version, String[] args) {
        super(name, version, args);
        options = new Options();
        context = new HashMap<>();
        commands = new HashMap<>();
    }

    Map<String, Object> getContext() {
        return context;
    }

    public void setValue(String key, Object value) {
        context.put(key, value);
    }

    public Object getValue(String key) {
        return context.get(key);
    }

    protected interface Command {
        void run();
    };

    public void addCommand(Option option, Command command) {
        commands.put(option, command);
        options.addOption(option);
    }

    protected abstract CommandLineParser getCommandLineParser();

    protected abstract void optionError(ParseException e);

    protected void parseCLIOptions() {
        CommandLine cmd;
        try {
            cmd = getCommandLineParser().parse(options, getArguments());
        } catch (ParseException e) {
            optionError(e);
            exit(-1);
        }

    }

    @Override
    public void run() {
        parseCLIOptions();
    }

    private Options options;
    private Map<String, Object> context;
    private Map<Option, Command> commands;
}

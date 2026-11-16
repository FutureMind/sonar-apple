/*
 * SonarQube Apple Plugin - Enables analysis of Swift and Objective-C projects into SonarQube.
 * Copyright © 2022 inside|app (contact@insideapp.fr)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package fr.insideapp.sonarqube.apple.commons.cli;

import org.buildobjects.process.ExternalProcessFailureException;
import org.buildobjects.process.ProcBuilder;
import org.buildobjects.process.StartupException;
import org.buildobjects.process.TimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Set;

public abstract class CommandLineToolRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(CommandLineToolRunner.class);
    private static final int COMMAND_TIMEOUT = 10 * 60 * 1000;
    private static final Integer DEFAULT_COMMAND_EXIT_CODE = 0;
    private final String command;

    protected CommandLineToolRunner(final String command) {
        this.command = command;
    }

    protected Integer[] exitCodes() {
        return new Integer[]{DEFAULT_COMMAND_EXIT_CODE};
    }

    /**
     * Directory the tool runs in. Tools that receive paths relative to the project
     * (sonar.sources, Periphery settings) must run in the project base directory.
     * @return null to inherit the scanner's working directory
     */
    protected File workingDirectory() {
        return null;
    }

    /**
     * Logs why the tool could not produce output, in a way that is actionable without --debug.
     */
    protected final void logFailure(Exception e) {
        if (e instanceof StartupException) {
            LOGGER.error("Could not start '{}'. Is it installed and on the PATH of the scanner?", command);
        } else if (e instanceof TimeoutException) {
            LOGGER.error("'{}' did not finish within {} minutes and was stopped.", command, COMMAND_TIMEOUT / 60_000);
        } else if (e instanceof ExternalProcessFailureException failure) {
            LOGGER.error("'{}' exited with code {}: {}", command, failure.getExitValue(), abbreviate(failure.getStderr()));
        } else {
            LOGGER.error("Running '{}' failed: {}", command, e.getMessage());
        }
        LOGGER.debug("'{}' failure details", command, e);
    }

    private static String abbreviate(String text) {
        if (text == null) {
            return "";
        }
        String trimmed = text.strip();
        return trimmed.length() > 500 ? trimmed.substring(0, 500) + "..." : trimmed;
    }

    private ProcBuilder build(String[] arguments) {
        ProcBuilder builtCommand = new ProcBuilder(command)
                .withArgs(arguments)
                .withTimeoutMillis(COMMAND_TIMEOUT)
                .withExpectedExitStatuses(Set.of(exitCodes()));
        File workingDirectory = workingDirectory();
        if (workingDirectory != null) {
            builtCommand.withWorkingDirectory(workingDirectory);
        }
        LOGGER.debug("Command that will be executed: {}", builtCommand.getCommandLine());
        return builtCommand;
    }

    public final String execute(String[] arguments) throws Exception {
        return build(arguments).run().getOutputString();
    }

}
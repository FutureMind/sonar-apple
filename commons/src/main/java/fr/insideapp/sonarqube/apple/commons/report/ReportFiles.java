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
package fr.insideapp.sonarqube.apple.commons.report;

import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.config.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Reads pre-generated tool reports listed in a "report paths" scanner property,
 * so a sensor can import them instead of running the tool itself.
 */
public final class ReportFiles {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReportFiles.class);

    private ReportFiles() {
    }

    /**
     * @return true when the report paths property holds at least one path
     */
    public static boolean configured(Configuration configuration, String reportPathsKey) {
        for (String path : configuration.getStringArray(reportPathsKey)) {
            if (!path.isBlank()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Reads every file listed (comma-separated) in the report paths property.
     * Relative paths are resolved against the project base directory.
     * Missing or unreadable files are logged and skipped, they never fail the analysis.
     *
     * @return the content of each readable report, in the configured order
     */
    public static List<String> read(SensorContext context, String reportPathsKey, String toolName) {
        Path baseDir = context.fileSystem().baseDir().toPath();
        List<String> contents = new ArrayList<>();
        for (String path : context.config().getStringArray(reportPathsKey)) {
            if (path.isBlank()) {
                continue;
            }
            Path resolved = baseDir.resolve(path.trim());
            if (!Files.isRegularFile(resolved)) {
                LOGGER.warn("{} report not found: {}", toolName, resolved);
                continue;
            }
            try {
                contents.add(Files.readString(resolved));
                LOGGER.info("Read {} report: {}", toolName, resolved);
            } catch (IOException e) {
                LOGGER.error("Failed to read {} report: {}", toolName, resolved, e);
            }
        }
        return contents;
    }

}

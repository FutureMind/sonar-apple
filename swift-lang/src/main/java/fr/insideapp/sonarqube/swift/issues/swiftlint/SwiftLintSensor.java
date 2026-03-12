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
package fr.insideapp.sonarqube.swift.issues.swiftlint;

import fr.insideapp.sonarqube.apple.commons.issues.ReportIssue;
import fr.insideapp.sonarqube.apple.commons.issues.ReportIssueRecorder;
import fr.insideapp.sonarqube.swift.Swift;
import fr.insideapp.sonarqube.swift.issues.swiftlint.mapper.SwiftLintReportMappable;
import fr.insideapp.sonarqube.swift.issues.swiftlint.models.SwiftLintIssue;
import fr.insideapp.sonarqube.swift.issues.swiftlint.parser.SwiftLintReportParsable;
import fr.insideapp.sonarqube.swift.issues.swiftlint.runner.SwiftLintRunnable;

import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.Sensor;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.api.config.Configuration;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SwiftLintSensor implements Sensor {

    private static final Logger LOGGER = Loggers.get(SwiftLintSensor.class);

    private final Swift swift;
    private final SwiftLintRulesDefinition rulesDefinition;
    private final SwiftLintRunnable runner;
    private final SwiftLintReportParsable parser;

    private final SwiftLintReportMappable mapper;

    public SwiftLintSensor(
            final Swift swift,
            final SwiftLintRulesDefinition rulesDefinition,
            final SwiftLintRunnable runner,
            final SwiftLintReportParsable parser,
            final SwiftLintReportMappable mapper
    ) {
        this.swift = swift;
        this.rulesDefinition = rulesDefinition;
        this.runner = runner;
        this.parser = parser;
        this.mapper = mapper;
    }

    @Override
    public void describe(SensorDescriptor sensorDescriptor) {
        sensorDescriptor
                .onlyOnLanguage(swift.getKey())
                .name("SwiftLint Sensor")
                .onlyOnFileType(InputFile.Type.MAIN);
    }

    @Override
    public void execute(SensorContext sensorContext) {
        Configuration config = sensorContext.config();
        Optional<String> reportPaths = config.get(SwiftLintExtensionProvider.REPORT_PATHS_KEY);

        List<String> outputs;
        if (reportPaths.isPresent()) {
            outputs = readReportFiles(reportPaths.get(), sensorContext);
        } else {
            outputs = runner.run();
        }

        List<SwiftLintIssue> issues = outputs.stream()
                .map(parser::parse)
                .flatMap(List::stream)
                .toList();
        List<ReportIssue> reportIssues = mapper.map(issues).stream().toList();
        ReportIssueRecorder issueRecorder = new ReportIssueRecorder();
        issueRecorder.recordIssues(reportIssues, rulesDefinition.getRepositoryKey(), sensorContext);
    }

    private List<String> readReportFiles(String paths, SensorContext sensorContext) {
        List<String> outputs = new ArrayList<>();
        Path baseDir = sensorContext.fileSystem().baseDir().toPath();
        for (String path : paths.split(",")) {
            Path resolved = baseDir.resolve(path.trim());
            if (Files.exists(resolved)) {
                try {
                    outputs.add(Files.readString(resolved));
                    LOGGER.info("Read SwiftLint report: {}", resolved);
                } catch (IOException e) {
                    LOGGER.error("Failed to read SwiftLint report: {}", resolved);
                    LOGGER.debug("{}", e);
                }
            } else {
                LOGGER.warn("SwiftLint report not found: {}", resolved);
            }
        }
        return outputs;
    }

}

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
package fr.insideapp.sonarqube.swift.issues.periphery;

import fr.insideapp.sonarqube.apple.commons.issues.ReportIssue;
import fr.insideapp.sonarqube.apple.commons.issues.ReportIssueRecorder;
import fr.insideapp.sonarqube.swift.Swift;
import fr.insideapp.sonarqube.swift.issues.periphery.mapper.PeripheryReportMappable;
import fr.insideapp.sonarqube.swift.issues.periphery.models.PeripheryIssue;
import fr.insideapp.sonarqube.swift.issues.periphery.parser.PeripheryReportParsable;
import fr.insideapp.sonarqube.swift.issues.periphery.runner.PeripheryRunnable;

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
import java.util.Objects;
import java.util.Optional;

public class PeripherySensor implements Sensor {

    private static final Logger LOGGER = Loggers.get(PeripherySensor.class);

    private final Swift swift;
    private final PeripheryRulesDefinition rulesDefinition;
    private final PeripheryRunnable runner;
    private final PeripheryReportParsable parser;

    private final PeripheryReportMappable mapper;

    public PeripherySensor(
            final Swift swift,
            final PeripheryRulesDefinition rulesDefinition,
            final PeripheryRunnable runner,
            final PeripheryReportParsable parser,
            final PeripheryReportMappable mapper
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
                .name("Periphery Sensor")
                .onlyOnFileType(InputFile.Type.MAIN);
    }

    @Override
    public void execute(SensorContext sensorContext) {
        if (sensorContext.config().getBoolean(PeripheryExtensionProvider.SKIP_KEY).orElse(false)) {
            LOGGER.info("Skipping Periphery analysis ({}=true).", PeripheryExtensionProvider.SKIP_KEY);
            return;
        }
        Configuration config = sensorContext.config();
        Optional<String> reportPaths = config.get(PeripheryExtensionProvider.REPORT_PATHS_KEY);

        List<String> outputs;
        if (reportPaths.isPresent()) {
            outputs = readReportFiles(reportPaths.get(), sensorContext);
        } else {
            outputs = List.of(runner.run());
        }

        List<PeripheryIssue> issues = outputs.stream()
                .map(parser::parse)
                .flatMap(List::stream)
                .filter(issue -> Objects.nonNull(issue.location))
                .toList();
        List<ReportIssue> reportIssues = new ArrayList<>(mapper.map(issues));
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
                    LOGGER.info("Read Periphery report: {}", resolved);
                } catch (IOException e) {
                    LOGGER.error("Failed to read Periphery report: {}", resolved);
                    LOGGER.debug("{}", e);
                }
            } else {
                LOGGER.warn("Periphery report not found: {}", resolved);
            }
        }
        return outputs;
    }

}

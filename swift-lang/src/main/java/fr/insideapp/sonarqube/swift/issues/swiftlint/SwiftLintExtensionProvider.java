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

import fr.insideapp.sonarqube.apple.commons.ExtensionProvider;
import fr.insideapp.sonarqube.swift.issues.swiftlint.mapper.SwiftLintReportMapper;
import fr.insideapp.sonarqube.swift.issues.swiftlint.parser.SwiftLintReportParser;
import fr.insideapp.sonarqube.swift.issues.swiftlint.runner.SwiftLintRunner;
import org.sonar.api.config.PropertyDefinition;
import org.sonar.api.scanner.ScannerSide;

import java.util.Arrays;
import java.util.List;

@ScannerSide
public final class SwiftLintExtensionProvider implements ExtensionProvider {

    public static final String REPORT_PATHS_KEY = "sonar.apple.swiftLint.reportPaths";

    private static final String SUBCATEGORY = "SwiftLint";

    private static final PropertyDefinition REPORT_PATHS_PROPERTY = PropertyDefinition
            .builder(REPORT_PATHS_KEY)
            .name("SwiftLint report paths")
            .description("Comma-separated paths to pre-generated SwiftLint JSON reports. When set, the sensor imports these instead of running the swiftlint binary.")
            .onConfigScopes(PropertyDefinition.ConfigScope.PROJECT)
            .category(APPLE_CATEGORY)
            .subCategory(SUBCATEGORY)
            .build();

    public List<Object> extensions() {
        return Arrays.asList(
                REPORT_PATHS_PROPERTY,
                SwiftLintRulesDefinition.class,
                SwiftLintRunner.class,
                SwiftLintReportParser.class,
                SwiftLintReportMapper.class,
                SwiftLintSensor.class
        );
    }

}

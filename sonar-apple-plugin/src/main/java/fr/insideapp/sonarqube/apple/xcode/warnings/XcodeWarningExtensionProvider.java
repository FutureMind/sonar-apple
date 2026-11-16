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
package fr.insideapp.sonarqube.apple.xcode.warnings;

import fr.insideapp.sonarqube.apple.commons.ExtensionProvider;
import fr.insideapp.sonarqube.apple.xcode.warnings.converter.XcodeWarningsConverter;
import fr.insideapp.sonarqube.apple.xcode.warnings.mapper.XcodeWarningsMapper;
import fr.insideapp.sonarqube.apple.xcode.warnings.parser.XcodeWarningParser;
import fr.insideapp.sonarqube.apple.xcode.warnings.splitter.XcodeWarningsReportIssueSplitter;
import fr.insideapp.sonarqube.objc.issues.warnings.XcodeWarningObjectiveCRulesDefinition;
import fr.insideapp.sonarqube.swift.issues.warnings.XcodeWarningSwiftRulesDefinition;
import org.sonar.api.config.PropertyDefinition;
import org.sonar.api.scanner.ScannerSide;

import java.util.Arrays;
import java.util.List;

@ScannerSide
public final class XcodeWarningExtensionProvider implements ExtensionProvider {

    private static final String CATEGORY = "Warnings";

    public static final String REPORT_PATHS_KEY = "sonar.apple.warnings.reportPaths";

    private static final PropertyDefinition REPORT_PATHS_PROPERTY = PropertyDefinition
            .builder(REPORT_PATHS_KEY)
            .name("Xcode Warnings report paths")
            .description("Comma-separated paths to pre-generated Xcode xcresulttool JSON reports. When set, the sensor imports these instead of running xcresulttool.")
            .onConfigScopes(PropertyDefinition.ConfigScope.PROJECT)
            .category(APPLE_CATEGORY)
            .subCategory(CATEGORY)
            .build();

    public List<Object> extensions() {
        return Arrays.asList(
            REPORT_PATHS_PROPERTY,
            XcodeWarningObjectiveCRulesDefinition.class,
            XcodeWarningSwiftRulesDefinition.class,
            XcodeWarningParser.class,
            XcodeWarningsConverter.class,
            XcodeWarningsMapper.class,
            XcodeWarningsReportIssueSplitter.class,
            XcodeWarningsSensor.class
        );
    }


}
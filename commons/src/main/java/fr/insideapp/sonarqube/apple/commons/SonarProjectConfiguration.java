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
package fr.insideapp.sonarqube.apple.commons;

import org.sonar.api.config.Configuration;
import org.sonar.api.scanner.ScannerSide;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@ScannerSide
public class SonarProjectConfiguration {

    private final Configuration configuration;

    public SonarProjectConfiguration(final Configuration configuration) {
        this.configuration = configuration;
    }

    public static final String PROJECT_BASE_DIR_KEY = "sonar.projectBaseDir";

    /**
     * @return the project base directory as set by the scanner, if known
     */
    public Optional<File> baseDir() {
        return configuration.get(PROJECT_BASE_DIR_KEY).filter(s -> !s.isBlank()).map(File::new);
    }

    public List<String> sources() {
        return Arrays.asList(configuration.getStringArray("sonar.sources"));
    }


}

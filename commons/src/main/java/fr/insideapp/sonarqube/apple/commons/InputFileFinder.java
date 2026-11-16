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

import org.sonar.api.batch.fs.FilePredicate;
import org.sonar.api.batch.fs.FilePredicates;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Maps a file path reported by an external tool (SwiftLint, xccov, xcresulttool...) to an indexed file.
 */
public final class InputFileFinder {

    private InputFileFinder() {
    }

    /**
     * @param path   absolute path, or path relative to the project base directory
     * @param filter additional constraint, e.g. only main files
     * @return the matching indexed file, or null. When the path as reported does not match,
     * its real path is tried, so symlinked locations still match (on macOS /tmp and /var are
     * symlinks to /private/tmp and /private/var).
     */
    public static InputFile find(FileSystem fileSystem, String path, FilePredicate filter) {
        FilePredicates predicates = fileSystem.predicates();
        InputFile inputFile = fileSystem.inputFile(predicates.and(
                predicates.or(predicates.hasAbsolutePath(path), predicates.hasRelativePath(path)),
                filter));
        if (inputFile != null) {
            return inputFile;
        }
        try {
            Path reported = Paths.get(path);
            if (!reported.isAbsolute() || !Files.exists(reported)) {
                return null;
            }
            Path real = reported.toRealPath();
            if (real.equals(reported)) {
                return null;
            }
            return fileSystem.inputFile(predicates.and(predicates.hasAbsolutePath(real.toString()), filter));
        } catch (IOException | InvalidPathException | SecurityException e) {
            return null;
        }
    }

}

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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.internal.DefaultInputFile;
import org.sonar.api.batch.fs.internal.TestInputFileBuilder;
import org.sonar.api.batch.sensor.internal.SensorContextTester;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

public final class InputFileFinderTest {

    @TempDir
    Path tempDir;

    private Path baseDir;
    private SensorContextTester context;
    private DefaultInputFile mainFile;

    @BeforeEach
    public void prepare() throws IOException {
        baseDir = Files.createDirectories(tempDir.resolve("project")).toRealPath();
        Files.createDirectories(baseDir.resolve("Sources"));
        Files.writeString(baseDir.resolve("Sources/App.swift"), "let a = 1\n");
        context = SensorContextTester.create(baseDir);
        mainFile = new TestInputFileBuilder("", "Sources/App.swift")
                .setModuleBaseDir(baseDir)
                .setType(InputFile.Type.MAIN)
                .build();
        context.fileSystem().add(mainFile);
    }

    @Test
    public void find_relativePath() {
        assertThat(InputFileFinder.find(context.fileSystem(), "Sources/App.swift", context.fileSystem().predicates().all()))
                .isEqualTo(mainFile);
    }

    @Test
    public void find_absolutePath() {
        assertThat(InputFileFinder.find(context.fileSystem(), baseDir.resolve("Sources/App.swift").toString(), context.fileSystem().predicates().all()))
                .isEqualTo(mainFile);
    }

    @Test
    public void find_throughSymlink() throws IOException {
        Path link = Files.createSymbolicLink(tempDir.resolve("link"), baseDir);
        assertThat(InputFileFinder.find(context.fileSystem(), link.resolve("Sources/App.swift").toString(), context.fileSystem().predicates().all()))
                .isEqualTo(mainFile);
    }

    @Test
    public void find_respectsFilter() {
        assertThat(InputFileFinder.find(context.fileSystem(), "Sources/App.swift", context.fileSystem().predicates().hasType(InputFile.Type.TEST)))
                .isNull();
    }

    @Test
    public void find_unknown() {
        assertThat(InputFileFinder.find(context.fileSystem(), "/does/not/exist.swift", context.fileSystem().predicates().all()))
                .isNull();
    }

}

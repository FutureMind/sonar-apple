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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.sonar.api.batch.sensor.internal.SensorContextTester;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

public final class ReportFilesTest {

    private static final String KEY = "sonar.apple.test.reportPaths";

    @TempDir
    Path baseDir;

    private SensorContextTester context;

    @BeforeEach
    public void prepare() {
        context = SensorContextTester.create(baseDir);
    }

    @Test
    public void configured_notSet() {
        assertThat(ReportFiles.configured(context.config(), KEY)).isFalse();
    }

    @Test
    public void configured_blank() {
        context.settings().setProperty(KEY, " , ");
        assertThat(ReportFiles.configured(context.config(), KEY)).isFalse();
    }

    @Test
    public void configured_set() {
        context.settings().setProperty(KEY, "report.json");
        assertThat(ReportFiles.configured(context.config(), KEY)).isTrue();
    }

    @Test
    public void read_relativeAndAbsolutePathsInOrder() throws IOException {
        Files.writeString(baseDir.resolve("first.json"), "first");
        Path absolute = Files.writeString(baseDir.resolve("second.json"), "second");
        context.settings().setProperty(KEY, "first.json, " + absolute);

        assertThat(ReportFiles.read(context, KEY, "Test")).containsExactly("first", "second");
    }

    @Test
    public void read_skipsMissingFilesAndDirectories() throws IOException {
        Files.writeString(baseDir.resolve("present.json"), "present");
        Files.createDirectory(baseDir.resolve("folder"));
        context.settings().setProperty(KEY, "missing.json,folder,present.json");

        assertThat(ReportFiles.read(context, KEY, "Test")).containsExactly("present");
    }

    @Test
    public void read_notSet() {
        assertThat(ReportFiles.read(context, KEY, "Test")).isEmpty();
    }

}

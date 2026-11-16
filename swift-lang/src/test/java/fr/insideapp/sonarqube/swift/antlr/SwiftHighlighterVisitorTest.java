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
package fr.insideapp.sonarqube.swift.antlr;

import fr.insideapp.sonarqube.apple.commons.antlr.CustomTreeVisitor;
import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.internal.TestInputFileBuilder;
import org.sonar.api.batch.sensor.highlighting.TypeOfText;
import org.sonar.api.batch.sensor.internal.SensorContextTester;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

public class SwiftHighlighterVisitorTest {

    private static final String TEST_ROOT = "src/test/resources/swift";
    private static final String TEST_FILENAME = "main.swift";

    @Test
    public void fillContext() throws IOException {
        SensorContextTester context = SensorContextTester.create(new File(TEST_ROOT));

        File file = new File(TEST_ROOT, TEST_FILENAME);

        InputFile testFile = new TestInputFileBuilder("foo", TEST_FILENAME)
                .setLanguage("swift")
                .setModuleBaseDir(Paths.get(TEST_ROOT))
                .setContents(FileUtils.readFileToString(file, Charset.defaultCharset()))
                .setCharset(Charset.defaultCharset())
                .build();
        context.fileSystem().add(testFile);
        SwiftAntlrContext antlrContext = new SwiftAntlrContext();
        antlrContext.loadFromStreams(
                testFile,
                testFile.inputStream(),
                testFile.inputStream(),
                testFile.charset()
        );

        SwiftHighlighterVisitor highlighterVisitor = new SwiftHighlighterVisitor();
        CustomTreeVisitor customTreeVisitor = new CustomTreeVisitor(highlighterVisitor);
        customTreeVisitor.fillContext(context, antlrContext);
        assertThat(context.highlightingTypeAt(testFile.key(), 2, 0)).containsExactlyInAnyOrder(TypeOfText.KEYWORD);
        assertThat(context.highlightingTypeAt(testFile.key(), 2, 10)).isEmpty();
        assertThat(context.highlightingTypeAt(testFile.key(), 3, 8)).containsExactlyInAnyOrder(TypeOfText.KEYWORD);
        assertThat(context.highlightingTypeAt(testFile.key(), 3, 13)).containsExactlyInAnyOrder(TypeOfText.KEYWORD);
        assertThat(context.highlightingTypeAt(testFile.key(), 3, 19)).containsExactlyInAnyOrder(TypeOfText.KEYWORD_LIGHT);
        assertThat(context.highlightingTypeAt(testFile.key(), 4, 20)).containsExactlyInAnyOrder(TypeOfText.STRING);
    }

    @Test
    public void fillContext_withCharactersOutsideBmp() throws IOException {
        // each emoji is 1 code point for ANTLR but 2 UTF-16 chars for SonarQube
        SensorContextTester context = highlight("let a = \"\uD83D\uDE00\uD83D\uDE00\uD83D\uDE00\"\nlet b = 1\n");
        String key = "foo:emoji.swift";
        assertThat(context.highlightingTypeAt(key, 1, 0)).containsExactlyInAnyOrder(TypeOfText.KEYWORD);
        // the string body spans UTF-16 columns 9..14 (3 surrogate pairs)
        assertThat(context.highlightingTypeAt(key, 1, 9)).containsExactlyInAnyOrder(TypeOfText.STRING);
        assertThat(context.highlightingTypeAt(key, 1, 14)).containsExactlyInAnyOrder(TypeOfText.STRING);
        // tokens on the next line are not shifted
        assertThat(context.highlightingTypeAt(key, 2, 0)).containsExactlyInAnyOrder(TypeOfText.KEYWORD);
        assertThat(context.highlightingTypeAt(key, 2, 4)).isEmpty();
    }

    private SensorContextTester highlight(String contents) throws IOException {
        SensorContextTester context = SensorContextTester.create(new File(TEST_ROOT));
        InputFile testFile = new TestInputFileBuilder("foo", "emoji.swift")
                .setLanguage("swift")
                .setModuleBaseDir(Paths.get(TEST_ROOT))
                .setContents(contents)
                .setCharset(StandardCharsets.UTF_8)
                .build();
        context.fileSystem().add(testFile);
        SwiftAntlrContext antlrContext = new SwiftAntlrContext();
        antlrContext.loadFromStreams(testFile, testFile.inputStream(), testFile.inputStream(), testFile.charset());
        new CustomTreeVisitor(new SwiftHighlighterVisitor()).fillContext(context, antlrContext);
        return context;
    }

}

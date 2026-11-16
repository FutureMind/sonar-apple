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

import org.apache.commons.io.input.BOMInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class SourceLinesProvider {
    private static final Logger LOGGER = LoggerFactory.getLogger(SourceLinesProvider.class);

    public SourceLine[] getLines(final InputStream inputStream, final Charset charset) {
        if (inputStream == null) {
            return new SourceLine[0];
        }
        final List<SourceLine> sourceLines = new ArrayList<>();

        try (final BufferedReader bufferedReader = new BufferedReader(
                new InputStreamReader(bomInputStream(inputStream), charset))) {
            // positions are counted in code points, like the ANTLR char streams they are matched against
            int totalLines = 1;
            int global = 0;
            int count = 0;
            final List<Integer> supplementaryColumns = new ArrayList<>();

            int currentChar;
            while ((currentChar = bufferedReader.read()) != -1) {
                if (Character.isHighSurrogate((char) currentChar)) {
                    // first half of a surrogate pair: the pair is a single code point
                    supplementaryColumns.add(count);
                    continue;
                }
                global++;
                count++;
                if (currentChar == 10) {
                    sourceLines.add(new SourceLine(totalLines, count, global - count, global, toArray(supplementaryColumns)));
                    totalLines++;
                    count = 0;
                    supplementaryColumns.clear();
                }
            }
            sourceLines.add(new SourceLine(totalLines, count, global - count, global, toArray(supplementaryColumns)));
        } catch (final Exception e) {
            LOGGER.warn("Error occurred reading file", e);
        }

        return sourceLines.toArray(new SourceLine[0]);
    }

    private static int[] toArray(final List<Integer> values) {
        return values.stream().mapToInt(Integer::intValue).toArray();
    }

    public BOMInputStream bomInputStream(final InputStream inputStream) throws IOException {
        return BOMInputStream.builder()
            .setInputStream(inputStream)
            .setInclude(false)
            .get();
    }

}

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
package fr.insideapp.sonarqube.apple.commons.parser;

import fr.insideapp.sonarqube.apple.commons.ExceptionHelper;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import static org.assertj.core.api.Assertions.assertThat;

public final class ReportListParserTest {

    private ReportListParser parser;

    @Before
    public void prepare() {
        parser = mock(ReportListParser.class, Mockito.CALLS_REAL_METHODS);
    }

    @Test
    public void parse_failed() throws Exception {
        // prepare
        when(parser.perform(anyString())).thenThrow(ExceptionHelper.build());
        // test
        List<?> outputs = parser.parse("{\"report\": true}");
        // assert
        assertThat(outputs).isEmpty();
    }

    @Test
    public void parse_blank_input() throws Exception {
        // test: a failed tool run yields empty output, which must not be parsed
        List<?> outputs = parser.parse("  ");
        // assert
        assertThat(outputs).isEmpty();
        verify(parser, never()).perform(anyString());
    }

    @Test
    public void parse_succeed() throws Exception {
        // prepare
        when(parser.perform(anyString())).thenReturn(Arrays.asList("string_parsed"));
        // test
        List<?> outputs = parser.parse("{\"report\": true}");
        // assert
        assertThat(outputs).hasSize(1);
        assertThat(outputs.get(0)).isEqualTo("string_parsed");
    }

}

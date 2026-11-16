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

import fr.insideapp.sonarqube.swift.antlr.generated.Swift5Lexer;
import fr.insideapp.sonarqube.swift.antlr.generated.Swift5Parser;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;

public final class SwiftSupportTest {

    // A postfix operator as the very last token (no trailing newline) used to throw
    // IndexOutOfBoundsException: bitIndex < 0 from SwiftSupport.isRightOperatorWS(EOF)
    @Test
    public void parse_operatorAtEndOfFile() {
        for (String source : new String[]{"let y = x!", "let y = x?", "x!", "let y = a + b"}) {
            Swift5Lexer lexer = new Swift5Lexer(CharStreams.fromString(source));
            lexer.removeErrorListeners();
            Swift5Parser parser = new Swift5Parser(new CommonTokenStream(lexer));
            parser.removeErrorListeners();
            assertThatCode(parser::top_level).as(source).doesNotThrowAnyException();
        }
    }

}

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

import org.junit.Before;
import org.junit.Test;
import org.sonar.api.config.Configuration;

import java.io.File;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class SonarProjectConfigurationTest {

    private Configuration configuration;

    private SonarProjectConfiguration projectConfiguration;

    @Before
    public void prepare() {
        configuration = mock(Configuration.class);
        projectConfiguration = new SonarProjectConfiguration(configuration);
    }

    @Test
    public void sources_not_specified() {
        // prepare
        when(configuration.getStringArray("sonar.sources")).thenReturn(new String[]{});
        // test
        List<String> sources = projectConfiguration.sources();
        // assert
        assertThat(sources).isEmpty();
    }

    @Test
    public void sources_one_value() {
        // prepare
        when(configuration.getStringArray("sonar.sources")).thenReturn(new String[]{"source"});
        // test
        List<String> sources = projectConfiguration.sources();
        // assert
        assertThat(sources).hasSize(1);
        assertThat(sources.get(0)).isEqualTo("source");
    }

    @Test
    public void sources_many_values() {
        // prepare
        when(configuration.getStringArray("sonar.sources")).thenReturn(new String[]{"source1","source2"});
        // test
        List<String> sources = projectConfiguration.sources();
        // assert
        assertThat(sources).hasSize(2);
        assertThat(sources.get(0)).isEqualTo("source1");
        assertThat(sources.get(1)).isEqualTo("source2");
    }

    @Test
    public void baseDir_specified() {
        when(configuration.get("sonar.projectBaseDir")).thenReturn(Optional.of("/projects/app"));
        assertThat(projectConfiguration.baseDir()).contains(new File("/projects/app"));
    }

    @Test
    public void baseDir_not_specified() {
        when(configuration.get("sonar.projectBaseDir")).thenReturn(Optional.empty());
        assertThat(projectConfiguration.baseDir()).isEmpty();
    }

}

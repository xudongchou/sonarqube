/*
 * SonarQube
 * Copyright (C) 2009-2021 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonar.server.platform.ws;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.sonar.server.health.CeStatusNodeCheck;
import org.sonar.server.health.DbConnectionNodeCheck;
import org.sonar.server.health.EsStatusNodeCheck;
import org.sonar.server.health.Health;
import org.sonar.server.health.WebServerStatusNodeCheck;
import org.sonar.server.platform.WebServer;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class LivenessCheckerImplTest {

  public static final Health RED = Health.newHealthCheckBuilder().setStatus(Health.Status.RED).build();

  private final WebServer webServer = mock(WebServer.class);
  private final DbConnectionNodeCheck dbConnectionNodeCheck = mock(DbConnectionNodeCheck.class);
  private final WebServerStatusNodeCheck webServerStatusNodeCheck = mock(WebServerStatusNodeCheck.class);
  private final CeStatusNodeCheck ceStatusNodeCheck = mock(CeStatusNodeCheck.class);
  private final EsStatusNodeCheck esStatusNodeCheck = mock(EsStatusNodeCheck.class);

  LivenessCheckerImpl underTest = new LivenessCheckerImpl(webServer, dbConnectionNodeCheck, webServerStatusNodeCheck, ceStatusNodeCheck, esStatusNodeCheck);

  @Test
  public void fail_when_db_connexion_check_fail() {
    when(dbConnectionNodeCheck.check()).thenReturn(RED);

    Assertions.assertThat(underTest.liveness()).isFalse();
  }

  @Test
  public void fail_when_web_check_fail() {
    when(dbConnectionNodeCheck.check()).thenReturn(Health.GREEN);
    when(webServerStatusNodeCheck.check()).thenReturn(RED);

    Assertions.assertThat(underTest.liveness()).isFalse();
  }

  @Test
  public void fail_when_ce_check_fail() {
    when(dbConnectionNodeCheck.check()).thenReturn(Health.GREEN);
    when(webServerStatusNodeCheck.check()).thenReturn(Health.GREEN);
    when(ceStatusNodeCheck.check()).thenReturn(RED);

    Assertions.assertThat(underTest.liveness()).isFalse();
  }

  @Test
  public void dont_fail_when_not_standalone_but_es_check_fail() {
    when(dbConnectionNodeCheck.check()).thenReturn(Health.GREEN);
    when(webServerStatusNodeCheck.check()).thenReturn(Health.GREEN);
    when(ceStatusNodeCheck.check()).thenReturn(Health.GREEN);
    when(webServer.isStandalone()).thenReturn(false);
    when(esStatusNodeCheck.check()).thenReturn(RED);

    Assertions.assertThat(underTest.liveness()).isTrue();
  }

  @Test
  public void fail_when_not_dce_and_es_check_fail() {
    when(dbConnectionNodeCheck.check()).thenReturn(Health.GREEN);
    when(webServerStatusNodeCheck.check()).thenReturn(Health.GREEN);
    when(ceStatusNodeCheck.check()).thenReturn(Health.GREEN);
    when(webServer.isStandalone()).thenReturn(true);
    when(esStatusNodeCheck.check()).thenReturn(RED);

    Assertions.assertThat(underTest.liveness()).isFalse();
  }

}

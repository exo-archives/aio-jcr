/*
 * Copyright (C) 2003-2008 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.jcr.benchmark.sessionregistry;

import org.exoplatform.jcr.benchmark.JCRTestBase;
import org.exoplatform.jcr.benchmark.JCRTestContext;
import org.exoplatform.services.jcr.core.CredentialsImpl;
import org.exoplatform.services.jcr.impl.core.RepositoryImpl;
import org.exoplatform.services.jcr.impl.core.SessionImpl;
import org.exoplatform.services.jcr.impl.core.SessionRegistry;

import com.sun.japex.TestCase;

/**
 * Created by The eXo Platform SAS.
 * 
 * @author <a href="mailto:anatoliy.bazko@exoplatform.com.ua">Anatoliy Bazko</a>
 * @version $Id: AbstractOrganizationTest.java 111 2008-11-11 11:11:11Z $
 */
public abstract class AbstractSessionRegistryTest extends JCRTestBase {

  protected final int       AGENT_COUNT = 100;

  protected CredentialsImpl credentials;

  protected RepositoryImpl  repository;

  protected SessionRegistry sessionRegistry;

  @Override
  public void doPrepare(TestCase tc, JCRTestContext context) throws Exception {
    super.doPrepare(tc, context);

    credentials = new CredentialsImpl("root", "exo".toCharArray());
    repository = (RepositoryImpl) context.getSession().getRepository();

    sessionRegistry = (SessionRegistry) ((SessionImpl) context.getSession()).getContainer()
                                                                            .getComponentInstanceOfType(SessionRegistry.class);
    sessionRegistry.start();
  }

  @Override
  public void doFinish(TestCase tc, JCRTestContext context) throws Exception {
    sessionRegistry.stop();

    super.doFinish(tc, context);
  }
}

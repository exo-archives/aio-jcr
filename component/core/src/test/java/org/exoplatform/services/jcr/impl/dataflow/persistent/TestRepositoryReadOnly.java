/*
 * Copyright (C) 2003-2007 eXo Platform SAS.
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

package org.exoplatform.services.jcr.impl.dataflow.persistent;

import org.exoplatform.services.jcr.JcrImplBaseTest;
import org.exoplatform.services.jcr.core.WorkspaceContainerFacade;
import org.exoplatform.services.jcr.dataflow.PlainChangesLogImpl;
import org.exoplatform.services.jcr.storage.WorkspaceDataContainer;

/**
 * Created by The eXo Platform SAS Author : Sergey Karpenko <sergey.karpenko@exoplatform.com.ua>
 * 
 * @version $Id$
 */

public class TestRepositoryReadOnly extends JcrImplBaseTest {

  @Override
  public void setUp() throws Exception {
    super.setUp();

    repository.getWorkspaceContainer(session.getWorkspace().getName());
    // dataContainer = (WorkspaceDataContainer) wsFacade.getComponent(WorkspaceDataContainer.class);
  }

  @Override
  protected void tearDown() throws Exception {
    repository.setState(repository.ONLINE);
    super.tearDown();
  }

  public void testRepositoryReadOnly() throws Exception {

    repository.setState(repository.READONLY);

    assertEquals(repository.READONLY, repository.getState());

    WorkspacePersistentDataManager dm = (WorkspacePersistentDataManager) (repository.getWorkspaceContainer(session.getWorkspace()
                                                                                                                  .getName())).getComponent(WorkspacePersistentDataManager.class);

    try {
      dm.save(new PlainChangesLogImpl());
      fail("Read-only container should throw an ReadOnlyWorkspaceException");
    } catch (ReadOnlyWorkspaceException e) {
      // ok
    }
  }

}

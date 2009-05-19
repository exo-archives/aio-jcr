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
import org.exoplatform.services.jcr.dataflow.PersistentDataManager;
import org.exoplatform.services.jcr.dataflow.PlainChangesLogImpl;
import org.exoplatform.services.jcr.storage.WorkspaceDataContainer;

/**
 * Created by The eXo Platform SAS.
 * 
 * <br/>Date: 15.08.2008
 * 
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a>
 * @version $Id$
 */
public class TestWorspaceReadOnly extends JcrImplBaseTest {

  private WorkspaceContainerFacade wsFacade;

  private PersistentDataManager dataManager;

  @Override
  public void setUp() throws Exception {
    super.setUp();

    wsFacade = repository.getWorkspaceContainer(session.getWorkspace().getName());
    dataManager = (PersistentDataManager) wsFacade.getComponent(PersistentDataManager.class);
  }

  @Override
  protected void tearDown() throws Exception {
    dataManager.setReadOnly(false);
    super.tearDown();
  }

  public void testWorkspaceDataContainerStatus() {

    dataManager.setReadOnly(true);

    assertTrue(dataManager.isReadOnly());
  }

  public void testWorkspaceDataManager() throws Exception {

    dataManager.setReadOnly(true);

    WorkspacePersistentDataManager dm = (WorkspacePersistentDataManager) wsFacade.getComponent(WorkspacePersistentDataManager.class);

    try {
      dm.save(new PlainChangesLogImpl());
      fail("Read-only container should throw an ReadOnlyWorkspaceException");
    } catch (ReadOnlyWorkspaceException e) {
      // ok
    }
  }

}

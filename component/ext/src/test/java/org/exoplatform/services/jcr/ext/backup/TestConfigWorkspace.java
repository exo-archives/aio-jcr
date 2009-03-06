/*
 * Copyright (C) 2003-2009 eXo Platform SAS.
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
package org.exoplatform.services.jcr.ext.backup;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import javax.jcr.RepositoryException;

import org.exoplatform.services.jcr.config.RepositoryConfigurationException;
import org.exoplatform.services.jcr.config.RepositoryEntry;
import org.exoplatform.services.jcr.config.RepositoryServiceConfiguration;
import org.exoplatform.services.jcr.config.WorkspaceEntry;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.BaseStandaloneTest;
import org.exoplatform.services.jcr.ext.metadata.MetaDataActionTest;
import org.exoplatform.services.jcr.impl.core.RepositoryImpl;
import org.jibx.runtime.BindingDirectory;
import org.jibx.runtime.IBindingFactory;
import org.jibx.runtime.IUnmarshallingContext;
import org.jibx.runtime.JiBXException;

/**
 * Created by The eXo Platform SAS.
 * 
 * <br/>Date: 27.02.2009
 * 
 * @author <a href="mailto:alex.reshetnyak@exoplatform.com.ua">Alex Reshetnyak</a>
 * @version $Id: TestRegistrateWorkspace.java 111 2008-11-11 11:11:11Z rainf0x $
 */
public class TestConfigWorkspace extends BaseStandaloneTest {

  public void testRestore() throws Exception {
    WorkspaceEntry wsEntry = getWorkspaceEntry(getStreamConfig(), repository.getName(), "ws77");

    ManageableRepository mr = repositoryService.getRepository("db3");

    mr.configWorkspace(wsEntry);

    assertEquals(false, workspaceAlreadyExist(repository.getName(), "ws77"));

    InputStream is = TestConfigWorkspace.class.getResourceAsStream("/backup/repository_backup-20090305_055624.xml");

    mr.importWorkspace(wsEntry.getName(), is);
  }

  private WorkspaceEntry getWorkspaceEntry(InputStream wEntryStream,
                                           String repositoryName,
                                           String workspaceName) throws FileNotFoundException,
                                                                JiBXException,
                                                                RepositoryConfigurationException {
    WorkspaceEntry wsEntry = null;

    IBindingFactory factory = BindingDirectory.getFactory(RepositoryServiceConfiguration.class);
    IUnmarshallingContext uctx = factory.createUnmarshallingContext();
    RepositoryServiceConfiguration conf = (RepositoryServiceConfiguration) uctx.unmarshalDocument(wEntryStream,
                                                                                                  null);

    RepositoryEntry rEntry = conf.getRepositoryConfiguration(repositoryName);

    for (WorkspaceEntry wEntry : rEntry.getWorkspaceEntries())
      if (wEntry.getName().equals(workspaceName))
        wsEntry = wEntry;

    if (wsEntry == null)
      throw new RuntimeException("Can not find the workspace '" + workspaceName
          + "' in configuration.");

    return wsEntry;
  }

  private InputStream getStreamConfig() throws FileNotFoundException {
    String containerConf = getClass().getResource("/conf/standalone/exo-jcr-config_for_TestBackupServer.xml")
                                     .getPath();
    return new FileInputStream(containerConf);
  }

  private boolean workspaceAlreadyExist(String repository, String workspace) throws RepositoryException,
                                                                            RepositoryConfigurationException {
    String[] ws = repositoryService.getRepository(repository).getWorkspaceNames();

    for (int i = 0; i < ws.length; i++)
      if (ws[i].equals(workspace))
        return true;
    return false;
  }
}

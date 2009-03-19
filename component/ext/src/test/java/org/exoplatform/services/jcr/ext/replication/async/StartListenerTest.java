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
package org.exoplatform.services.jcr.ext.replication.async;

import java.io.File;
import java.util.Iterator;

import org.exoplatform.services.jcr.dataflow.ItemState;
import org.exoplatform.services.jcr.ext.BaseStandaloneTest;
import org.exoplatform.services.jcr.ext.replication.async.storage.LocalStorageImpl;
import org.exoplatform.services.jcr.impl.Constants;
import org.exoplatform.services.jcr.impl.util.io.FileCleaner;

/**
 * Created by The eXo Platform SAS.
 * 
 * <br/>Date: 15.01.2009
 * 
 * @author <a href="mailto:alex.reshetnyak@exoplatform.com.ua">Alex Reshetnyak</a>
 * @version $Id: AsyncReplicationTest.java 111 2008-11-11 11:11:11Z rainf0x $
 */
public class StartListenerTest extends BaseStandaloneTest {

  public void testAddedRootNode() throws Exception {
    File localDirPerWorkspace = new File("target/asyncreplication/local" + File.separator + "db1"
        + File.separator + "ws3");
    localDirPerWorkspace.mkdirs();

    LocalStorageImpl localStorage = new LocalStorageImpl(localDirPerWorkspace.getAbsolutePath(),
                                                         new FileCleaner(false));

    localStorage.onStart(null);
    Iterator<ItemState> changes = localStorage.getLocalChanges().getChanges();
    assertTrue(changes.hasNext());
    assertTrue(ItemState.isSame(changes.next(),
                                Constants.ROOT_UUID,
                                Constants.ROOT_PATH,
                                ItemState.ADDED));
  }
}

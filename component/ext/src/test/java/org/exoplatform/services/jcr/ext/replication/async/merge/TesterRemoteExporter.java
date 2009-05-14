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
package org.exoplatform.services.jcr.ext.replication.async.merge;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import org.exoplatform.services.jcr.dataflow.ItemState;
import org.exoplatform.services.jcr.dataflow.PlainChangesLog;
import org.exoplatform.services.jcr.dataflow.PlainChangesLogImpl;
import org.exoplatform.services.jcr.dataflow.serialization.ObjectWriter;
import org.exoplatform.services.jcr.ext.replication.async.RemoteExportException;
import org.exoplatform.services.jcr.ext.replication.async.RemoteExportResponce;
import org.exoplatform.services.jcr.ext.replication.async.RemoteExporter;
import org.exoplatform.services.jcr.ext.replication.async.storage.ChangesStorage;
import org.exoplatform.services.jcr.ext.replication.async.storage.ItemStatesStorage;
import org.exoplatform.services.jcr.ext.replication.async.storage.ResourcesHolder;
import org.exoplatform.services.jcr.ext.replication.async.storage.SimpleOutputChangesFile;
import org.exoplatform.services.jcr.ext.replication.async.transport.MemberAddress;
import org.exoplatform.services.jcr.impl.dataflow.serialization.ItemStateWriter;
import org.exoplatform.services.jcr.impl.dataflow.serialization.ObjectWriterImpl;

/**
 * Created by The eXo Platform SAS.
 * 
 * <br/>
 * Date: 11.12.2008
 * 
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a>
 * @version $Id$
 */
public class TesterRemoteExporter implements RemoteExporter {

  private PlainChangesLog changes;

  /**
   * Wrapped RemoteExporter.
   * 
   */
  public TesterRemoteExporter(PlainChangesLog changes) {
    this.changes = changes;
  }

  /**
   * Empty RemoteExporter.
   * 
   */
  public TesterRemoteExporter() {
    this.changes = new PlainChangesLogImpl("sessionId");
  }

  /**
   * {@inheritDoc}
   */
  public ChangesStorage<ItemState> exportItem(String nodeId) throws RemoteExportException {

    ChangesStorage<ItemState> chs = null;

    try {

      long timestamp = System.currentTimeMillis();
      try {

        // TODO CHANGE ChangesFile naming system!!!!!!
        Thread.sleep(100);
      } catch (InterruptedException e) {
      }
      File file = File.createTempFile("exportStor", Long.toString(timestamp));

      byte[] crc = new byte[] {}; // crc is ignored
      SimpleOutputChangesFile chfile = new SimpleOutputChangesFile(file,
                                                                   crc,
                                                                   timestamp,
                                                                   new ResourcesHolder());

      ObjectWriter out = new ObjectWriterImpl(chfile.getOutputStream());

      Iterator<ItemState> it = changes.getAllStates().iterator();

      ItemStateWriter wr = new ItemStateWriter();
      while (it.hasNext()) {
        wr.write(out, it.next());

      }
      out.close();

      chs = new ItemStatesStorage<ItemState>(chfile, null, null, 200 * 1024, null); // TODO member
    } catch (IOException e) {
      throw new RemoteExportException(e);
    }
    return chs;
  }

  public void setChanges(PlainChangesLog changes) {
    this.changes = changes;
  }

  /**
   * {@inheritDoc}
   */
  public void onRemoteExport(RemoteExportResponce event) {
    // dummy
  }

  public void setRemoteMember(MemberAddress address) {
    // dummy
  }

  /**
   * {@inheritDoc}
   */
  public void cleanup() {
    // dummy
  }

}

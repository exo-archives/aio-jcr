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

import java.io.IOException;

import org.exoplatform.services.jcr.dataflow.ItemState;
import org.exoplatform.services.jcr.dataflow.PlainChangesLog;
import org.exoplatform.services.jcr.dataflow.PlainChangesLogImpl;
import org.exoplatform.services.jcr.dataflow.TransactionChangesLog;
import org.exoplatform.services.jcr.ext.replication.async.RemoteExportException;
import org.exoplatform.services.jcr.ext.replication.async.RemoteExportResponce;
import org.exoplatform.services.jcr.ext.replication.async.RemoteExporter;
import org.exoplatform.services.jcr.ext.replication.async.storage.ChangesStorage;
import org.exoplatform.services.jcr.ext.replication.async.transport.Member;

/**
 * Created by The eXo Platform SAS.
 * 
 * <br/>Date: 11.12.2008
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
  public ChangesStorage<ItemState> exportItem(String nodetId) throws RemoteExportException {
    TesterChangesStorage<ItemState> chs = new TesterChangesStorage<ItemState>(new Member(null, -1));
    try {
      chs.addLog(new TransactionChangesLog(changes));
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

  public void setMember(Member address) {
    // dummy
  }
}

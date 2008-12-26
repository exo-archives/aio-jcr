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

import java.util.Iterator;

import org.exoplatform.services.jcr.dataflow.ItemState;
import org.exoplatform.services.jcr.dataflow.ItemStateChangesLog;
import org.exoplatform.services.jcr.dataflow.PlainChangesLogImpl;
import org.exoplatform.services.jcr.ext.replication.async.RemoteExportException;
import org.exoplatform.services.jcr.ext.replication.async.RemoteExportResponce;
import org.exoplatform.services.jcr.ext.replication.async.RemoteExporter;
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

  private final ItemStateChangesLog changes;

  /**
   * Wrapped RemoteExporter.
   * 
   */
  TesterRemoteExporter(ItemStateChangesLog changes) {
    this.changes = changes;
  }

  /**
   * Empty RemoteExporter.
   * 
   */
  TesterRemoteExporter() {
    this.changes = new PlainChangesLogImpl();
  }

  /**
   * {@inheritDoc}
   */
  public Iterator<ItemState> exportItem(String nodetId) throws RemoteExportException {
    return changes.getAllStates().iterator();
  }

  /**
   * {@inheritDoc}
   */
  public void onRemoteExport(RemoteExportResponce event) {
    // dummy
  }

  public void setMember(Member address) {
    // TODO Auto-generated method stub
    
  }
}

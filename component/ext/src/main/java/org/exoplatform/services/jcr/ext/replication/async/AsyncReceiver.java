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
package org.exoplatform.services.jcr.ext.replication.async;

import org.exoplatform.services.jcr.ext.replication.async.transport.AbstractPacket;
import org.exoplatform.services.jcr.ext.replication.async.transport.AsyncPacketListener;
import org.jgroups.Address;


/**
 * Created by The eXo Platform SAS.
 * 
 * <br/>Date: 12.12.2008
 * 
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a>
 * @version $Id$
 */
public interface AsyncReceiver extends AsyncPacketListener {

  /**
   * Handle remote get export request.
   *
   */
  void onGetExport(AbstractPacket packet, Address srcAddress);

  
  /**
   * Handle remote changes receive.
   *
   */
  void onChanges(AbstractPacket packet);
  
  /**
   * Set RemoteChangesListener for export.
   *
   * @param listener RemoteChangesListener.
   */
  void setRemoteChangesListener(RemoteExportClient listener);
  
  /**
   * Remove RemoteChangesListener for export.
   *
   */
  void removeRemoteChangesListener();

}

/**
 * 
 */
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

import java.io.IOException;

import org.exoplatform.services.jcr.dataflow.ItemStateChangesLog;

/**
 * Created by The eXo Platform SAS.
 * 
 * Handles remote item export requests. Returns item copy from a remote host.
 * Remote item will be returned as <code>ItemStateChangesLog</code>. 
 * 
 * <br/>Date: 11.12.2008
 *
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a> 
 * @version $Id$
 */
public interface RemoteExporter extends RemoteChangesListener {

  /**
   * Set exporter side priority.
   *
   * @param remotePriority int
   */
  void setPriority(int remotePriority);
  
  /**
   * Exports remote item and return chnages log with ADD states.
   * @param nodetId String
   * @param remotePriority int
   *
   * @return ItemStateChangesLog
   */
  ItemStateChangesLog exportItem(String nodetId) throws IOException;
  
}

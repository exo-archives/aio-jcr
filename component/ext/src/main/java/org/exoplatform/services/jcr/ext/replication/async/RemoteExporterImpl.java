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
import org.exoplatform.services.jcr.dataflow.PlainChangesLogImpl;

/**
 * Created by The eXo Platform SAS.
 * 
 * <br/>Date: 11.12.2008
 *
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a> 
 * @version $Id$
 */
public class RemoteExporterImpl implements RemoteExporter {

  protected final AsyncTransmitter transmitter;
  
  protected final AsyncReceiver receiver;
  
  /**
   * Remote priority.
   * Mutable value. Will be changed by Merge manager on each memebers pair merge.
   */
  protected int remotePriority;
  
  RemoteExporterImpl(AsyncTransmitter transmitter, AsyncReceiver receiver) {
    this.transmitter = transmitter;
    this.receiver = receiver;
    // this.receiver setListener
  }
  
  public void setPriority(int remotePriority) {
    this.remotePriority = remotePriority;  
  }
  
  /**
   * {@inheritDoc}
   */
  public ItemStateChangesLog exportItem(String nodetId) throws IOException {
    
    // send request
    transmitter.sendGetExport(nodetId, remotePriority);
    
    // TODO lock and wait for responce, error or timeout 
    
    return new PlainChangesLogImpl(); // TODO return responce changes 
  }

  /**
   * {@inheritDoc}
   */
  public void onRemoteExport(RemoteChangesEvent event) {
    // get responce - remote changes
    if (event.getCommand().equals("REMOTE_EXPORT_BLAH-BLAH")) { // TODO 
      // TODO cooperate with exportItem lock waiter
    }
  }  
}

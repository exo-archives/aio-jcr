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
import java.util.List;

import org.exoplatform.services.jcr.ext.replication.async.storage.ChangesFile;
import org.exoplatform.services.jcr.ext.replication.async.transport.Member;

/**
 * Created by The eXo Platform SAS.
 * 
 * <br/>Date: 12.12.2008
 * 
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a>
 * @version $Id$
 */
public interface AsyncTransmitter {

  /**
   * Send changes.
   * 
   * @param changes
   * @param subscribers
   * @throws IOException
   */
  void sendChanges(List<ChangesFile> changes, List<Member> subscribers) throws IOException;

  /**
   * Send Get Export request.
   * 
   * @param nodeId
   * @param address
   * @throws IOException
   */
  void sendGetExport(String nodeId, Member address) throws IOException;

  /**
   * Send export response.
   * 
   * @param changes
   * @param address
   * @throws IOException
   */
  void sendExport(ChangesFile changes, Member address) throws IOException;

  /**
   * sendError.
   *
   * @param error
   * @param address
   * @throws IOException
   */
  void sendError(String error, Member address) throws IOException;
  
  /**
   * send 'Done'.
   *
   * @throws IOException
   */
  void sendMerge() throws IOException;
  
  /**
   * send 'Cancel'.
   *
   * @throws IOException
   */
  void sendCancel() throws IOException;

}

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

import java.util.HashMap;

import org.exoplatform.services.jcr.ext.replication.async.storage.ChangesFile;
import org.exoplatform.services.jcr.ext.replication.async.transport.ChangesPacket;


/**
 * Created by The eXo Platform SAS.
 * 
 * <br/>Date: 24 Ãðó 2008
 *
 * @author <a href="karpenko.sergiy@gmail.com">Karpenko Sergiy</a> 
 * @version $Id: ChangesSubscriberImpl.java 111 2008-11-11 11:11:11Z serg $
 */
public class ChangesSubscriberImpl implements ChangesSubscriber {

  /**
   * Map with CRC key and RandomAccess File
   */
  HashMap<String, ChangesFile> incomChanges;
  
  public ChangesSubscriberImpl(){
    incomChanges = new HashMap<String, ChangesFile>();
  }
  
  public void onChanges(ChangesPacket packet) {
    //TODO

  }

}

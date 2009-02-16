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
package org.exoplatform.services.jcr.impl.dataflow.serialization;

import org.exoplatform.services.jcr.access.AccessControlList;
import org.exoplatform.services.jcr.dataflow.ItemState;
import org.exoplatform.services.jcr.dataflow.PlainChangesLogImpl;
import org.exoplatform.services.jcr.dataflow.TransactionChangesLog;
import org.exoplatform.services.jcr.dataflow.serialization.JCRExternalizable;
import org.exoplatform.services.jcr.dataflow.serialization.UnknownClassIdException;
import org.exoplatform.services.jcr.impl.dataflow.TransientNodeData;
import org.exoplatform.services.jcr.impl.dataflow.TransientPropertyData;
import org.exoplatform.services.jcr.impl.dataflow.TransientValueData;

/**
 * Created by The eXo Platform SAS.
 * 
 * <br/>Date: 13.02.2009
 * 
 * @author <a href="mailto:alex.reshetnyak@exoplatform.com.ua">Alex Reshetnyak</a>
 * @version $Id: JCRExternlizableFactory.java 111 2008-11-11 11:11:11Z rainf0x $
 */
public class JCRExternlizableFactory {

  private final class Contstants {
    public static final int TRANSIENT_VALUE_DETA    = 1;

    public static final int ACCESS_CONTROL_LIST     = 2;

    public static final int ITEM_STATE              = 3;

    public static final int PLAIN_CHANGES_LOG_IMPL  = 4;

    public static final int TRANSACTION_CHANGES_LOG = 5;
    
    public static final int TRANSIENT_NODE_DATA     = 7;
    
    public static final int TRANSIENT_PROPERTY_DATA = 8;
    
  }

  public static int getObjectId(JCRExternalizable object) throws UnknownClassIdException {
    if (object instanceof TransientValueData) {
      return Contstants.TRANSIENT_VALUE_DETA;
    } else if (object instanceof AccessControlList) {
      return Contstants.ACCESS_CONTROL_LIST;
    } else if (object instanceof ItemState) {
      return Contstants.ITEM_STATE;
    } else if (object instanceof PlainChangesLogImpl) {
      return Contstants.PLAIN_CHANGES_LOG_IMPL;
    } else if (object instanceof TransactionChangesLog) {
      return Contstants.TRANSACTION_CHANGES_LOG;
    } else if (object instanceof TransientNodeData) {
      return Contstants.TRANSIENT_NODE_DATA;
    } else if (object instanceof TransientPropertyData) {
      return Contstants.TRANSIENT_PROPERTY_DATA;
    } else
      throw new UnknownClassIdException(object + " - has no bounded class instanse.");
  }

  public static JCRExternalizable getObjectInstanse(int id) throws UnknownClassIdException {
    switch (id) {
    case (Contstants.TRANSIENT_VALUE_DETA):
      return new TransientValueData();
    case (Contstants.ACCESS_CONTROL_LIST):
      return new AccessControlList();
    case (Contstants.ITEM_STATE):
      return new ItemState();
    case (Contstants.PLAIN_CHANGES_LOG_IMPL):
      return new PlainChangesLogImpl();
    case (Contstants.TRANSACTION_CHANGES_LOG):
      return new TransactionChangesLog();
    case (Contstants.TRANSIENT_NODE_DATA):
      return new TransientNodeData();
    case (Contstants.TRANSIENT_PROPERTY_DATA):
      return new TransientPropertyData();
    default:
      throw new UnknownClassIdException(id + " - has no bounded class instanse.");
    }
  }
}

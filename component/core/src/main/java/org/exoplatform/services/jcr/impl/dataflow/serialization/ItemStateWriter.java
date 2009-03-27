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

import java.io.IOException;

import org.exoplatform.services.jcr.dataflow.ItemState;
import org.exoplatform.services.jcr.dataflow.serialization.SerializationConstants;
import org.exoplatform.services.jcr.dataflow.serialization.ObjectWriter;
import org.exoplatform.services.jcr.datamodel.ItemData;
import org.exoplatform.services.jcr.impl.dataflow.TransientNodeData;
import org.exoplatform.services.jcr.impl.dataflow.TransientPropertyData;


/**
 * Created by The eXo Platform SAS.
 * 
 * <br/>Date: 
 *
 * @author <a href="karpenko.sergiy@gmail.com">Karpenko Sergiy</a> 
 * @version $Id: ItemStateWriter.java 111 2008-11-11 11:11:11Z serg $
 */
public class ItemStateWriter{


  public void write(ObjectWriter out, ItemState itemState) throws IOException {
    // write id
    out.writeInt(SerializationConstants.ITEM_STATE);
    
    out.writeInt(itemState.getState());
    out.writeBoolean(itemState.isPersisted());
    out.writeBoolean(itemState.isEventFire());
    
    // write flag isNodeData and ItemData
    ItemData data = itemState.getData();
    
    boolean isNodeData = (data instanceof TransientNodeData);
    out.writeBoolean(isNodeData);
    if(isNodeData){
      TransientNodeDataWriter wr = new TransientNodeDataWriter();
      wr.write(out, (TransientNodeData)data);
    }else{
      TransientPropertyDataWriter wr = new TransientPropertyDataWriter();
      wr.write(out, (TransientPropertyData)data);
    }
  }

}

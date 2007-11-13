/**
 * Copyright 2001-2007 The eXo Platform SAS         All rights reserved. 
 * Please look at license.txt in info directory for more license detail.  
 */

package org.exoplatform.services.jcr.impl.dataflow.replication;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * Created by The eXo Platform SAS Author : Alex Reshetnyak
 * alex.reshetnyak@exoplatform.com.ua 24.11.2006
 * 
 * @author <a href="mailto:alex.reshetnyak@exoplatform.com.ua">Alex Reshetnyak</a>
 * @version $Id$
 */

public class FixupStream implements Externalizable{
  int iItemStateId = -1;
  int iValueDataId = -1;
  
  public FixupStream(){}
  
  public FixupStream(int itemState_, int valueData_){
    iItemStateId = itemState_;
    iValueDataId = valueData_;
  }
  
  public int getItemSateId(){
    return iItemStateId;
  }
  
  public int getValueDataId (){
    return iValueDataId;
  }
  
  public boolean compare(FixupStream fs){
    boolean b = true;
    if (fs.getItemSateId() != this.getItemSateId())
      b = false;
    if (fs.getValueDataId() != this.getValueDataId())
      b = false;
    return b;
  }

  public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
    iItemStateId = in.readInt();
    iValueDataId = in.readInt();
  }

  public void writeExternal(ObjectOutput out) throws IOException {
    out.writeInt(iItemStateId);
    out.writeInt(iValueDataId);
  }
}
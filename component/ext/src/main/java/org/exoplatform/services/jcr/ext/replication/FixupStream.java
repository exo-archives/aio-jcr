/*
 * Copyright (C) 2003-2007 eXo Platform SAS.
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
package org.exoplatform.services.jcr.ext.replication;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * Created by The eXo Platform SAS Author : Alex Reshetnyak
 * alex.reshetnyak@exoplatform.com.ua 24.11.2006
 * 
 * @author <a href="mailto:alex.reshetnyak@exoplatform.com.ua">Alex Reshetnyak</a>
 * @version $Id: FixupStream.java 8440 2007-11-30 15:52:29Z svm $
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
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
package org.exoplatform.services.jcr.ext.replication.async.storage;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.exoplatform.services.jcr.dataflow.ItemState;

/**
 * Created by The eXo Platform SAS. <br/>Date: 24.12.2008
 * 
 * @author <a href="mailto:alex.reshetnyak@exoplatform.com.ua">Alex Reshetnyak</a>
 * @version $Id: ItemSatesiterator.java 111 2008-11-11 11:11:11Z rainf0x $
 */
public class ItemStateIterator<T extends ItemState> implements Iterator<T> {

  private final ChangesFile changesFile;

  private ItemState          prereadedItem;

  private ObjectInputStream  in;

  public ItemStateIterator(ChangesFile changesFile) {
    this.changesFile = changesFile;

    try{
      this.in = new ObjectInputStream(changesFile.getDataStream());
      this.prereadedItem = getItemState(in);
    }catch(IOException e){
      
    }
  }

  public boolean hasNext() {
    return prereadedItem != null;
  }

  public T next() throws NoSuchElementException {
    if (prereadedItem == null)
      throw new NoSuchElementException();

    ItemState retVal = prereadedItem;
    prereadedItem = getItemState(in);
   
    return (T)retVal;
  }

  public void remove() {
    // TODO Auto-generated method stub
  }

  protected ItemState getItemState(ObjectInputStream in){
    ItemState elem=null;
    try{
      elem = (ItemState) in.readObject();
    }catch(ClassNotFoundException e){
      //TODO 
    }catch(EOFException e){
      // End of list
      elem = null;
    }catch(IOException e){
      //TODO 
    }
    return elem;
  }

}

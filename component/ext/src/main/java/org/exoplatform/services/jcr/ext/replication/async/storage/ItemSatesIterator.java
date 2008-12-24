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

import java.util.Iterator;

import org.exoplatform.services.jcr.dataflow.ItemState;

/**
 * Created by The eXo Platform SAS.
 * 
 * <br/>Date: 24.12.2008
 *
 * @author <a href="mailto:alex.reshetnyak@exoplatform.com.ua">Alex Reshetnyak</a> 
 * @version $Id: ItemSatesiterator.java 111 2008-11-11 11:11:11Z rainf0x $
 */
public class ItemSatesIterator<T extends ItemState> implements Iterator<T> {
  
  private ChangesFile changesFile;
  
  public ItemSatesIterator(ChangesFile changesFile) {
    this.changesFile = changesFile;
  }

  public boolean hasNext() {
    // TODO Auto-generated method stub
    return false;
  }

  public T next() {
    // TODO Auto-generated method stub
    return null;
  }

  public void remove() {
    // TODO Auto-generated method stub
    
  }

}

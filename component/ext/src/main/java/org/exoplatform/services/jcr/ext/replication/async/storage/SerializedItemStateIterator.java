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
import org.exoplatform.services.jcr.ext.replication.async.RemoteExportException;

/**
 * Created by The eXo Platform SAS.
 * 
 * <br/>Date: 26.12.2008
 *
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a> 
 * @version $Id$
 */
public class SerializedItemStateIterator<T extends ItemState> implements Iterator<T> {

  private final ObjectInputStream in;
  
  private final ChangesFile changesFile;
  
  private T                 nextItem;
  

  /**
   * SerializedItemStateIterator  constructor.
   *
   * @param dataStream InputStream
   * @throws RemoteExportException if error occurs
   */
  public SerializedItemStateIterator(ChangesFile changesFile) throws RemoteExportException {
    this.changesFile = changesFile;
    try {
      this.in = new ObjectInputStream(this.changesFile.getDataStream());
      this.nextItem = readNext();
    } catch (IOException e) {
      throw new RemoteExportException(e);
    } catch (ClassNotFoundException e) {
      throw new RemoteExportException(e);
    } catch (ClassCastException e) {
      throw new RemoteExportException(e);
    }
  }
  
  /**
   * SerializedItemStateIterator  constructor - for TESTS.
   *
   */
  public SerializedItemStateIterator() {
    this.changesFile = null;
    this.in = null;
  }

  /**
   * {@inheritDoc}
   */
  public boolean hasNext() {
    return nextItem != null;
  }

  /**
   * {@inheritDoc}
   */
  public T next() throws NoSuchElementException {
    if (nextItem == null)
      throw new NoSuchElementException();

    T retVal = nextItem;
    try {
      nextItem = readNext();
    } catch (IOException e) {
      throw new NoSuchElementException(e.getMessage());
    } catch (ClassNotFoundException e) {
      throw new NoSuchElementException(e.getMessage());
    } catch (ClassCastException e) {
      throw new NoSuchElementException(e.getMessage());
    }

    return retVal;
  }

  /**
   * {@inheritDoc}
   */
  public void remove() {
    throw new RuntimeException("Remove not implemented");
  }

  @SuppressWarnings("unchecked")
  protected T readNext() throws IOException, ClassNotFoundException, ClassCastException {
    try {
      return (T) in.readObject();
    } catch (EOFException e) {
      // End of list
      in.close();
      return null;
    }
  }

  /**
   * Returns ChangesFile which objects are iterated by this iterator.
   * 
   * @return ChangesFile
   */
  public ChangesFile getChangesFile(){
    return changesFile;
  }
  
}

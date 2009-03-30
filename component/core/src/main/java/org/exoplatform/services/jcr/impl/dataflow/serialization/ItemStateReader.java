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
import org.exoplatform.services.jcr.dataflow.serialization.ObjectReader;
import org.exoplatform.services.jcr.dataflow.serialization.SerializationConstants;
import org.exoplatform.services.jcr.dataflow.serialization.UnknownClassIdException;
import org.exoplatform.services.jcr.impl.util.io.FileCleaner;

/**
 * Created by The eXo Platform SAS. <br/>Date:
 * 
 * @author <a href="karpenko.sergiy@gmail.com">Karpenko Sergiy</a>
 * @version $Id: ItemStateReader.java 111 2008-11-11 11:11:11Z serg $
 */
public class ItemStateReader {

  private FileCleaner fileCleaner;

  private int         maxBufferSize;
  private ReaderSpoolFileHolder holder;


  /**
   * Constructor.
   */
  public ItemStateReader(FileCleaner fileCleaner, int maxBufferSize, ReaderSpoolFileHolder holder ) {
    this.fileCleaner = fileCleaner;
    this.maxBufferSize = maxBufferSize;
    this.holder = holder;
  }

  /**
   * Read and set ItemState data.
   * 
   * @param in ObjectReader.
   * @return ItemState object.
   * @throws UnknownClassIdException If read Class ID is not expected or do not
   *           exist.
   * @throws IOException If an I/O error has occurred.
   */
  public ItemState read(ObjectReader in) throws UnknownClassIdException, IOException {
    // read id
    int key;
    if ((key = in.readInt()) != SerializationConstants.ITEM_STATE) {
      throw new UnknownClassIdException("There is unexpected class [" + key + "]");
    }

    int state = in.readInt();
    boolean isPersisted = in.readBoolean();
    boolean eventFire = in.readBoolean();

    boolean isNodeData = in.readBoolean();

    ItemState is;
    if (isNodeData) {
      TransientNodeDataReader rdr = new TransientNodeDataReader();
      is = new ItemState(rdr.read(in), state, eventFire, null, false, isPersisted);
    } else {
      TransientPropertyDataReader rdr = new TransientPropertyDataReader(fileCleaner, maxBufferSize, holder);
      is = new ItemState(rdr.read(in), state, eventFire, null, false, isPersisted);
    }
    return is;
  }

}

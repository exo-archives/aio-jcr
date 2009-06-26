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
import java.util.ArrayList;
import java.util.List;

import org.exoplatform.services.jcr.dataflow.ItemState;
import org.exoplatform.services.jcr.dataflow.PlainChangesLog;
import org.exoplatform.services.jcr.dataflow.PlainChangesLogImpl;
import org.exoplatform.services.jcr.dataflow.serialization.ObjectReader;
import org.exoplatform.services.jcr.dataflow.serialization.SerializationConstants;
import org.exoplatform.services.jcr.dataflow.serialization.UnknownClassIdException;
import org.exoplatform.services.jcr.impl.util.io.FileCleaner;

/**
 * Created by The eXo Platform SAS. <br/>
 * Date:
 * 
 * @author <a href="karpenko.sergiy@gmail.com">Karpenko Sergiy</a>
 * @version $Id: PlainChangesLogReader.java 111 2008-11-11 11:11:11Z serg $
 */
public class PlainChangesLogReader {

  /**
   * File cleaner.
   */
  private FileCleaner           fileCleaner;

  /**
   * Maximum buffer size.
   */
  private int                   maxBufferSize;

  /**
   * Spool file holder.
   */
  private ReaderSpoolFileHolder holder;

  /**
   * PlainChangesLogReader constructor.
   * 
   * @param fileCleaner
   *          File cleaner
   * @param maxBufferSize
   *          maximum buffer size
   * @param holder
   *          Spool file holder
   */
  public PlainChangesLogReader(FileCleaner fileCleaner,
                               int maxBufferSize,
                               ReaderSpoolFileHolder holder) {
    this.fileCleaner = fileCleaner;
    this.maxBufferSize = maxBufferSize;
    this.holder = holder;
  }

  /**
   * Read and set PlainChangesLog data.
   * 
   * @param in
   *          ObjectReader.
   * @return PlainChangesLog object.
   * @throws UnknownClassIdException
   *           If read Class ID is not expected or do not exist.
   * @throws IOException
   *           If an I/O error has occurred.
   */
  public PlainChangesLog read(ObjectReader in) throws UnknownClassIdException, IOException {
    int key;
    if ((key = in.readInt()) != SerializationConstants.PLAIN_CHANGES_LOG_IMPL) {
      throw new UnknownClassIdException("There is unexpected class [" + key + "]");
    }
    int eventType = in.readInt();

    String sessionId = in.readString();

    List<ItemState> items = new ArrayList<ItemState>();
    int listSize = in.readInt();
    for (int i = 0; i < listSize; i++) {
      ItemStateReader isr = new ItemStateReader(fileCleaner, maxBufferSize, holder);
      ItemState is = isr.read(in);
      items.add(is);
    }

    return new PlainChangesLogImpl(items, sessionId, eventType);
  }

}

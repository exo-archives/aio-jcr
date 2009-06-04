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

import java.io.File;

import org.exoplatform.services.jcr.impl.util.io.SpoolFile;

/**
 * Created by The eXo Platform SAS. <br/>
 * Date:
 * 
 * @author <a href="karpenko.sergiy@gmail.com">Karpenko Sergiy</a>
 * @version $Id$
 */
public class SerializationSpoolFile extends SpoolFile {

  /**
   * SpoolFileHolder.
   */
  private final ReaderSpoolFileHolder holder;

  /**
   * Read SpoolFile id.
   */
  private final String                id;

  /**
   * Constructor.
   * 
   * @param parent
   *          parent directory.
   * @param id
   *          ReadedSpoolFiel id.
   * @param holder
   *          ReaderSpoolFileHolder.
   */
  public SerializationSpoolFile(File parent, String id, ReaderSpoolFileHolder holder) {
    super(parent, id);
    this.holder = holder;
    this.id = id;
  }

  /**
   * Return spoolFile id.
   * 
   * @return spoolFile id
   */
  public String getId() {
    return id;
  }

  @Override
  public synchronized boolean delete() {
    boolean result = super.delete();
    if (result) {
      holder.remove(id);
    }
    return result;
  }

}

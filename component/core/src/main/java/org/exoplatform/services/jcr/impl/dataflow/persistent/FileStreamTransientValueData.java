/**
 * 
 */
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
package org.exoplatform.services.jcr.impl.dataflow.persistent;

import java.io.File;
import java.io.IOException;

import javax.jcr.RepositoryException;

import org.exoplatform.services.jcr.impl.dataflow.TransientValueData;

/**
 * Created by The eXo Platform SAS.
 * 
 * <br/>
 * Class for Persistent ValueData in Session (transient) level. Methods getSpoolFile(),
 * setSpoolFile() should don't get/set persistent file. Method createTransientCopy() returns this
 * object.
 * 
 * <br/>
 * Date: 09.06.2009
 * 
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a>
 * @version $Id$
 */
public class FileStreamTransientValueData extends TransientValueData {

  /**
   * FileStreamTransientValueData constructor.
   * 
   * @param file
   *          File from Value storage
   * @param orderNumber
   *          int
   * @throws IOException
   *           if error occurs
   */
  FileStreamTransientValueData(File file, int orderNumber) throws IOException {
    super(orderNumber, null, null, file, null, -1, null, false);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public TransientValueData createTransientCopy() throws RepositoryException {
    return this;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public File getSpoolFile() {
    return null;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public long getLength() {
    return spoolFile.length();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setPersistetFile(File spoolFile) {
    assert !true : "Set of persistet file is out of contract.";
  }

  /**
   * {@inheritDoc}
   */
  public boolean isTransient() {
    return false;
  }

}

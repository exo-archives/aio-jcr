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

package org.exoplatform.services.jcr.impl.dataflow.persistent;

import java.io.IOException;

import javax.jcr.RepositoryException;

import org.exoplatform.services.jcr.impl.dataflow.TransientValueData;
import org.exoplatform.services.jcr.impl.util.io.FileCleaner;
import org.exoplatform.services.jcr.impl.util.io.SwapFile;

/**
 * Created by The eXo Platform SAS.
 * Implementation of FileStream ValueData secures deleting file in object finalization 
 * @author Gennady Azarenkov
 * @version $Id$
 */

public class CleanableFileStreamValueData extends FileStreamPersistedValueData {

  protected final FileCleaner cleaner;
  
  public CleanableFileStreamValueData(SwapFile file, int orderNumber, FileCleaner cleaner) {
    super(file, orderNumber, false);
    this.cleaner = cleaner;
  }

  protected void finalize() throws Throwable {
    cleaner.addFile(file);
  }

  public TransientValueData createTransientCopy() throws RepositoryException {
    try {
      return new TransientValueData(orderNumber, null, null, 
        file, cleaner, -1, null, false);
    } catch (IOException e) {
      throw new RepositoryException(e);
    }
  }

}

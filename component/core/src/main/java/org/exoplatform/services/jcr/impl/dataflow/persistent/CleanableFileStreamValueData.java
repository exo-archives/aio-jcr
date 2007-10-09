/**
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.services.jcr.impl.dataflow.persistent;

import java.io.IOException;

import javax.jcr.RepositoryException;

import org.exoplatform.services.jcr.impl.dataflow.TransientValueData;
import org.exoplatform.services.jcr.impl.util.io.FileCleaner;
import org.exoplatform.services.jcr.impl.util.io.SwapFile;

/**
 * Created by The eXo Platform SARL        .
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

/**
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.services.jcr.impl.dataflow.persistent;

import java.io.File;

import org.exoplatform.services.jcr.impl.dataflow.TransientValueData;
import org.exoplatform.services.jcr.impl.util.io.FileCleaner;

/**
 * Created by The eXo Platform SARL        .
 * Implementation of FileStream ValueData secures deleting file in object finalization 
 * @author Gennady Azarenkov
 * @version $Id: CleanableFileStreamValueData.java 12841 2007-02-16 08:58:38Z peterit $
 */

public class CleanableFileStreamValueData extends FileStreamPersistedValueData {

  protected final FileCleaner cleaner;
  
  public CleanableFileStreamValueData(File file, int orderNumber, boolean temp, FileCleaner cleaner) {
    super(file, orderNumber, temp);
    this.cleaner = cleaner;
  }

  protected void finalize() throws Throwable {
    cleaner.addFile(file);
  }

  public TransientValueData createTransientCopy() {
    return new TransientValueData(orderNumber, null, null, 
        file, cleaner, -1, null, false);
  }

}

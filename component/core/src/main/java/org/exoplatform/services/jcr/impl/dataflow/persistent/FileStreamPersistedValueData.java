/**
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.services.jcr.impl.dataflow.persistent;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.exoplatform.services.jcr.impl.dataflow.AbstractValueData;
import org.exoplatform.services.jcr.impl.dataflow.TransientValueData;

/**
 * Created by The eXo Platform SARL        .
 * @author Gennady Azarenkov
 * @version $Id$
 */

public class FileStreamPersistedValueData extends AbstractValueData {
  

  protected final File file;
  protected final boolean temp;

  public FileStreamPersistedValueData(File file, int orderNumber, boolean temp) {
    super(orderNumber);
    this.file = file;
    this.temp = temp;
  }
  
  /**
   *  @see org.exoplatform.services.jcr.datamodel.AbstractValueData#getAsStream()
   */
  public InputStream getAsStream() throws IOException {
    return new FileInputStream(file);
  }
  
  /**
   * @see org.exoplatform.services.jcr.datamodel.AbstractValueData#getAsByteArray()
   */
  public byte[] getAsByteArray() throws IllegalStateException {
    throw new IllegalStateException("It is illegal to call on FileStreamPersistedValueData due to potential lack of memory");
  }
  
  /**
   * @see org.exoplatform.services.jcr.datamodel.AbstractValueData#getLength()
   */
  public long getLength() {
    return file.length();
  }
  
  /**
   * @see org.exoplatform.services.jcr.datamodel.AbstractValueData#isByteArray()
   */
  public boolean isByteArray() {
    return false;
  }
  
  @Override
  public TransientValueData createTransientCopy() {
    return new TransientValueData(orderNumber, null, null, 
        file, null, -1, null, false);
  }


  protected void finalize() throws Throwable {
    try {
      if(temp && !file.delete())
        log.warn("FilePersistedValueData could not remove temporary file on finalize "+file.getAbsolutePath());
    } finally {
      super.finalize();
    }
  }
}

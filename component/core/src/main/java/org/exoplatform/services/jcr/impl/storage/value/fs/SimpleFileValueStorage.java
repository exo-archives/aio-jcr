/**
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.services.jcr.impl.storage.value.fs;

import java.io.IOException;

import org.exoplatform.services.jcr.storage.value.ValueIOChannel;

/**
 * Created by The eXo Platform SAS .
 * 
 * @author <a href="mailto:gennady.azarenkov@exoplatform.com">Gennady Azarenkov</a>
 * @version $Id: SimpleFileValueStorage.java 13463 2007-03-16 09:17:29Z geaz $
 */

public class SimpleFileValueStorage extends FileValueStorage {
  
  /** 
   * @see org.exoplatform.services.jcr.storage.value.ValueStoragePlugin#openIOChannel()
   */
  public ValueIOChannel openIOChannel() throws IOException {
    return new SimpleFileIOChannel(rootDir, cleaner, getId());
  }

}

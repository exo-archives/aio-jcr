/**
 * Copyright 2001-2007 The eXo Platform SAS         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.services.jcr.impl.storage.value.fs;

import java.io.IOException;

import org.exoplatform.services.jcr.storage.value.ValueIOChannel;

/**
 * Created by The eXo Platform SARL        .
 * @author Gennady Azarenkov
 * @version $Id: $
 */

public class TreeFileValueStorage extends FileValueStorage {

  
  @Override
  public ValueIOChannel openIOChannel() throws IOException {
    return new TreeFileIOChannel(rootDir, cleaner);
  }

}

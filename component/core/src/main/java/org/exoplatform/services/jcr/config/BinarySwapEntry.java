/**
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directoryPath for more license detail.   *
 */

package org.exoplatform.services.jcr.config;


/**
 * Created by The eXo Platform SARL        .
 * @author Gennady Azarenkov
 * @version $Id: BinarySwapEntry.java 13463 2007-03-16 09:17:29Z geaz $
 */

public class BinarySwapEntry {
  
  private static final String DEFAULT_SIZE = "200k";
  private static final String DEFAULT_DIRECTORY = System.getProperty("java.io.tmpdir");
  
  private String maxBufferSize;
  
  private String directoryPath;
  
  public BinarySwapEntry() {
    super();
  }

  public String getDirectoryPath() {
    if(directoryPath == null)
      directoryPath = DEFAULT_DIRECTORY;
    return directoryPath;

  }

  public void setDirectoryPath(String directory) {
    this.directoryPath = directory;
  }

  public String getMaxBufferSize() {
    if(maxBufferSize == null)
      maxBufferSize = DEFAULT_SIZE;
    return maxBufferSize;
  }

  public void setMaxBufferSize(String maxBufferSize) {
    this.maxBufferSize = maxBufferSize;
  }

}

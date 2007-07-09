/**
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.services.jcr.impl.util.io;
/**
 * Created by The eXo Platform SARL        . <br/>
 * per workspace container file cleaner holder object
 * @author Gennady Azarenkov
 * @version $Id: WorkspaceFileCleanerHolder.java 12841 2007-02-16 08:58:38Z peterit $
 */

public class WorkspaceFileCleanerHolder {
  
  private final FileCleaner fileCleaner;
  
  public WorkspaceFileCleanerHolder() {
    this.fileCleaner = new FileCleaner();
  }
  
  public FileCleaner getFileCleaner() {
    return fileCleaner;
  }

}

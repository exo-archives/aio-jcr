/**
 * Copyright 2001-2007 The eXo Platform SAS         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.services.jcr.impl.storage.value.fs;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.exoplatform.services.jcr.config.RepositoryConfigurationException;
import org.exoplatform.services.jcr.datamodel.PropertyData;
import org.exoplatform.services.jcr.impl.util.io.FileCleaner;
import org.exoplatform.services.jcr.storage.WorkspaceStorageConnection;
import org.exoplatform.services.jcr.storage.value.ValueStoragePlugin;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SARL        .
 * @author Gennady Azarenkov
 * @version $Id: $
 */

public abstract class FileValueStorage extends ValueStoragePlugin {

  protected static Log log = ExoLogger.getLogger("jcr.SimpleFileValueStorage");

  public final static String PATH = "path";

  protected File rootDir;

  protected final FileCleaner cleaner;

  public FileValueStorage() {
    this.cleaner = new FileCleaner();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.exoplatform.services.jcr.storage.value.ValueStoragePlugin#init(java.util.Properties)
   */
  public void init(Properties props) throws IOException,
      RepositoryConfigurationException {
    prepareRootDir(props.getProperty(PATH));
  }


  /*
   * (non-Javadoc)
   * 
   * @see org.exoplatform.services.jcr.storage.value.ValueStoragePlugin#checkConsistency(org.exoplatform.services.jcr.storage.WorkspaceStorageConnection)
   */
  public void checkConsistency(WorkspaceStorageConnection dataConnection) {

  }

  /*
   * (non-Javadoc)
   * 
   * @see org.exoplatform.services.jcr.storage.value.ValueStoragePlugin#match(java.lang.String,
   *      org.exoplatform.services.jcr.datamodel.PropertyData, int)
   */
  @Override
  public boolean match(String valueDataDescriptor, PropertyData prop,
      int valueOrderNumer) {
    return valueDataDescriptor.startsWith(rootDir.getAbsolutePath());
  }

  protected void prepareRootDir(String rootDirPath) throws IOException,
      RepositoryConfigurationException {
    this.rootDir = new File(rootDirPath);
    if (!rootDir.exists()) {
      if (rootDir.mkdirs())
        log.info("Directory created: " + rootDir.getAbsolutePath());
      else
        log.warn("Directory IS NOT created: " + rootDir.getAbsolutePath());
    } else {
      if (!rootDir.isDirectory())
        throw new RepositoryConfigurationException(
            "File exists but is not a directory " + rootDirPath);
    }
  }
}

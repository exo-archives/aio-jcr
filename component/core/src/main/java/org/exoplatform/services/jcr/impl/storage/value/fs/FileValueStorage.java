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
 * Created by The eXo Platform SAS.
 * @author Gennady Azarenkov
 * @version $Id: FileValueStorage.java 11907 2008-03-13 15:36:21Z ksm $
 */

public abstract class FileValueStorage extends ValueStoragePlugin {

  private Log log = ExoLogger.getLogger("jcr.FileValueStorage");

  public final static String PATH = "path";

  protected File rootDir;

  protected FileCleaner cleaner;

  public FileValueStorage() {
    this.cleaner = new FileCleaner();
  }

  /** 
   * @see org.exoplatform.services.jcr.storage.value.ValueStoragePlugin#init(java.util.Properties)
   */
  public void init(Properties props) throws IOException,
      RepositoryConfigurationException {
    prepareRootDir(props.getProperty(PATH));
  }

  /** 
   * @see org.exoplatform.services.jcr.storage.value.ValueStoragePlugin#checkConsistency(org.exoplatform.services.jcr.storage.WorkspaceStorageConnection)
   */
  public void checkConsistency(WorkspaceStorageConnection dataConnection) {

  }

  /** 
   * @see org.exoplatform.services.jcr.storage.value.ValueStoragePlugin#match(java.lang.String,
   *      org.exoplatform.services.jcr.datamodel.PropertyData, int)
   */
  @Override
  public boolean match(String storageId, PropertyData prop) {
    return getId().equals(storageId);
  }

  protected void prepareRootDir(String rootDirPath) throws IOException,
      RepositoryConfigurationException {
    this.rootDir = new File(rootDirPath);
    
    if (!rootDir.exists()) {
      if (rootDir.mkdirs()) {
        log.info("Directory created: " + rootDir.getAbsolutePath());
      } else {
        log.warn("Directory IS NOT created: " + rootDir.getAbsolutePath());
      }
    } else {
      if (!rootDir.isDirectory()) {
        throw new RepositoryConfigurationException(
            "File exists but is not a directory " + rootDirPath);
      }
    }
  }
}

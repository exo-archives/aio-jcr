/**
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
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
import org.exoplatform.services.jcr.storage.value.ValueIOChannel;
import org.exoplatform.services.jcr.storage.value.ValueStoragePlugin;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SARL .
 * 
 * @author <a href="mailto:gennady.azarenkov@exoplatform.com">Gennady Azarenkov</a>
 * @version $Id: SimpleFileValueStorage.java 13463 2007-03-16 09:17:29Z geaz $
 */

public class SimpleFileValueStorage extends ValueStoragePlugin {
  protected static Log        log  = ExoLogger.getLogger("jcr.SimpleFileValueStorage");

  public final static String  PATH = "path";

  private File                rootDir;

  protected final FileCleaner cleaner;

// private class DeletedFilesFilter implements FileFilter {
//    
// public boolean accept(File file) {
// return file.getName().endsWith(StorageFile.DELETED_EXTENSION);
// }
// }

  public SimpleFileValueStorage() {
    this.cleaner = new FileCleaner();
  }

  public void init(Properties props) throws IOException, RepositoryConfigurationException {
    prepareRootDir(props.getProperty(PATH));
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
        throw new RepositoryConfigurationException("File exists but is not a directory "
            + rootDirPath);
    }
  }

  public ValueIOChannel openIOChannel() throws IOException {
    return new FileIOChannel(rootDir, cleaner);
  }

  public void checkConsistency(WorkspaceStorageConnection dataConnection) {

  }

  @Override
  public boolean match(String valueDataDescriptor, PropertyData prop, int valueOrderNumer) {
    return valueDataDescriptor.startsWith(rootDir.getAbsolutePath());
  }

// public void checkConsistency(WorkspaceStorageConnection dataConnection) {
// File[] deletedBefore = rootDir.listFiles(new DeletedFilesFilter());
// if (deletedBefore != null)
// for (File markerFile: deletedBefore) {
// int delExtSize = StorageFile.DELETED_EXTENSION.length() + 1;
// String markerPath = markerFile.getAbsolutePath();
// File dataFile = new File(markerPath.substring(0, markerPath.length() -
// delExtSize));
// if (dataFile.exists()) {
// // check value is not exists
// String markerName = markerFile.getName();
// String propertyId = markerName.substring(0, markerName.length() -
// delExtSize);
// try {
// ItemData item = dataConnection.getItemData(propertyId);
// if (item == null) {
// if (!dataFile.delete())
// log.warn("Can't delete 'phantom' data file " + dataFile.getAbsolutePath());
// else
// log.info("A 'phantom' data file deleted " + dataFile.getAbsolutePath());
// } else {
// log.warn("Item data marked as deleted exists in repository with propertyId "
// + propertyId
// + ". The 'phantom' marker file will be deleted but data file doesn't.");
// }
// if (!markerFile.delete())
// log.warn("Can't delete 'phantom' marker file " +
// markerFile.getAbsolutePath());
// } catch(RepositoryException e) {
// log.error("Value file storage repository error of 'phantom' item data (" +
// propertyId + ") check. " + e.getMessage());
// } catch(IllegalStateException e) {
// log.error("Value file storage error of 'phantom' item data (" + propertyId +
// ") check. " + e.getMessage());
// }
// } else {
// log.warn("Delete 'phantom' marker file (no 'phantom' file exists) " +
// markerFile.getAbsolutePath());
// if (!markerFile.delete())
// log.warn("Can't delete 'phantom' marker file (no 'phantom' file exists) " +
// markerFile.getAbsolutePath());
// }
// }
// }
}

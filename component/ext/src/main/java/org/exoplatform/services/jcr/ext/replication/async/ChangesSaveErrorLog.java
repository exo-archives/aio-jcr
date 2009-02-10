/*
 * Copyright (C) 2003-2009 eXo Platform SAS.
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
package org.exoplatform.services.jcr.ext.replication.async;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.exoplatform.services.jcr.impl.Constants;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SAS.
 * 
 * ChangesSaveErrorLog will be saved the errors when saving changes from the other
 * members. ChangesSaveErrorLog will be used in ChangesSubscriberImpl.
 * 
 * <br/>Date: 27.01.2009
 * 
 * @author <a href="mailto:alex.reshetnyak@exoplatform.com.ua">Alex Reshetnyak</a>
 * @version $Id: SaveErrorLog.java 111 2008-11-11 11:11:11Z rainf0x $
 */
public class ChangesSaveErrorLog {

  private static final Log    LOG                   = ExoLogger.getLogger("jcr.ChangesSaveErrorLog");

  private static final String ERROR_FILENAME_SUFFIX = "errors";

  private final String        serviceDir;

  private final String        repositoryName;

  private final String        workcspaceName;

  public ChangesSaveErrorLog(String serviceDir, String repositoryName, String workspaceName) {
    this.serviceDir = serviceDir;
    this.repositoryName = repositoryName;
    this.workcspaceName = workspaceName;
  }

  /**
   * Return list of error messages. Can be 0.
   * 
   * @return array of String, returns zero-length array if no errors occurred.
   * @throws IOException
   */
  public String[] getErrors() throws IOException {
    File err = new File(serviceDir, getErrorFileName());
    if (!err.exists()) {
      return new String[0];
    } else {
      List<String> list = new ArrayList<String>();

      // Open reader
      BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(err),
                                                                   Constants.DEFAULT_ENCODING));
      String s;
      while ((s = br.readLine()) != null) {
        list.add(s);
      }
      br.close();
      return list.toArray(new String[list.size()]);
    }
  }

  public String getErrorLog() {
    return new File(serviceDir, getErrorFileName()).getAbsolutePath();
  }
  
  
  /**
   * Add exception in exception storage.
   * 
   * @param t
   *          Throwable
   */
  public void reportError(Throwable t) {
    try {
      File errorFile = new File(serviceDir + File.separator + getErrorFileName());

      if (!errorFile.exists())
        errorFile.createNewFile();

      BufferedWriter errorOut = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(errorFile,
                                                                                               true),
                                                                          Constants.DEFAULT_ENCODING));

      errorOut.write(t.getMessage() + "\n");
      errorOut.flush();
      errorOut.close();

    } catch (IOException ex) {
      LOG.error("Exception on write to error in file: ", ex);
    }
  }

  private String getErrorFileName() {
    return repositoryName + "_" + workcspaceName + "." + ERROR_FILENAME_SUFFIX;
  }

}

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
package org.exoplatform.jcr.backupconsole;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by The eXo Platform SAS. <br/>Date:
 * 
 * @author <a href="karpenko.sergiy@gmail.com">Karpenko Sergiy</a>
 * @version $Id: BackupClient.java 111 2008-11-11 11:11:11Z serg $
 */
public interface BackupClient {

  /**
   * Start Backup.
   * 
   * @param pathToWS path to repository and workspace.
   * @return String result.
   * @throws IOException transport exception.
   * @throws BackupExecuteException backup client internal exception.
   */
  public String startBackUp(String pathToWS) throws IOException, BackupExecuteException;

  /**
   * Start Incremental Backup.
   * 
   * @param pathToWS path to repository and workspace.
   * @param incr incemental job period.
   * @param jobnumber incremental job number.
   * @return String result.
   * @throws IOException transport exception.
   * @throws BackupExecuteException backup client internal exception.
   */
  public String startIncrementalBackUp(String pathToWS, long incr, int jobnumber) throws IOException,
                                                                                 BackupExecuteException;

  /**
   * Get Status.
   * 
   * @param pathToWS path to repository and workspace.
   * @return String result.
   * @throws IOException transport exception.
   * @throws BackupExecuteException
   */
  public String status(String pathToWS) throws IOException, BackupExecuteException;

  /**
   * Restore repository from backup file.
   * 
   * @param pathToWS path to repository and workspace.
   * @param pathToBackup path to backup file on server side.
   * @param config TODO
   * @return String result.
   * @throws IOException transport exception.
   * @throws BackupExecuteException backup client internal exception.
   */
  public String restore(String pathToWS, String pathToBackup, InputStream config) throws IOException,
                                                             BackupExecuteException;

  /**
   * Stop backup.
   * 
   * @param pathToWS path to repository and workspace.
   * @return String result.
   * @throws IOException transport exception.
   * @throws BackupExecuteException backup client internal exception.
   */
  public String stop(String pathToWS) throws IOException, BackupExecuteException;
  
  /**
   * Drop backup.
   * 
   * @param pathToWS path to repository and workspace.
   * @return String result.
   * @throws IOException transport exception.
   * @throws BackupExecuteException backup client internal exception.
   */
  public String drop(String pathToWS) throws IOException, BackupExecuteException;

}

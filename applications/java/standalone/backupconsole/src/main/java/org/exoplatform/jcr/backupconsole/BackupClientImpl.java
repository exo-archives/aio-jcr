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
import java.nio.ByteBuffer;

import org.apache.ws.commons.util.Base64;

/**
 * Created by The eXo Platform SAS. <br/>Date:
 * 
 * @author <a href="karpenko.sergiy@gmail.com">Karpenko Sergiy</a>
 * @version $Id: BackupClientImpl.java 111 2008-11-11 11:11:11Z serg $
 */
public class BackupClientImpl implements BackupClient {
  /**
   * The base path to this service.
   */
  public static final String BASE_URL = "/rest/backup-server";

  /**
   * Definition the operation types.
   */
  public static final class OperationType {
    /**
     * Full backup only operation.
     */
    public static final String FULL_BACKUP_ONLY     = "fullOnly";

    /**
     * Full and incremental backup operations.
     */
    public static final String FULL_AND_INCREMENTAL = "fullAndIncremental";

    /**
     * Restore operations.
     */
    public static final String RESTORE              = "restore";

    /**
     * Stop backup operations.
     */
    public static final String STOP_BACKUP          = "stopBackup";

    /**
     * The backup status operations.
     */
    public static final String GET_STATUS           = "getStatus";

    /**
     * OperationType constructor.
     */
    private OperationType() {
    }
  }

  /**
   * Client transport.
   */
  private ClientTransport transport;

  /**
   * User login.
   */
  private final String    userName;

  /**
   * User password.
   */
  private final String    pass;

  /**
   * Constructor.
   * 
   * @param transport ClientTransport implementation.
   * @param login user login.
   * @param pass user password.
   */
  public BackupClientImpl(ClientTransport transport, String login, String pass) {
    this.transport = transport;
    this.userName = login;
    this.pass = pass;
  }

  /**
   * {@inheritDoc}
   */
  public String startBackUp(String pathToWS) throws IOException, BackupExecuteException {
    String sURL = BASE_URL + pathToWS + "/" + userName + "/" + pass + "/"
        + OperationType.FULL_BACKUP_ONLY;
    return transport.execute(sURL);

  }

  /**
   * {@inheritDoc}
   */
  public String startIncrementalBackUp(String pathToWS, long incr, int jobnumber) throws IOException,
                                                                                 BackupExecuteException {
    String sURL = BASE_URL + pathToWS + "/" + userName + "/" + pass + "/" + incr + "/" + jobnumber
        + "/" + OperationType.FULL_AND_INCREMENTAL;

    return transport.execute(sURL);
  }

  /**
   * {@inheritDoc}
   */
  public String status(String pathToWS) throws IOException, BackupExecuteException {
    String sURL = BASE_URL + pathToWS + "/" + userName + "/" + pass + "/"
        + OperationType.GET_STATUS;
    return transport.execute(sURL);
  }

  /**
   * {@inheritDoc}
   */
  public String stop(String pathToWS) throws IOException, BackupExecuteException {
    String sURL = BASE_URL + pathToWS + "/" + userName + "/" + pass + "/"
        + OperationType.STOP_BACKUP;
    return transport.execute(sURL);
  }

  /**
   * {@inheritDoc}
   */
  public String restore(String pathToWS, String pathToBackup, InputStream config) throws IOException,
                                                                                 BackupExecuteException {
    String encodedPath = Base64.encode(pathToBackup.getBytes("UTF-8"),
                                       0,
                                       (int) pathToBackup.getBytes("UTF-8").length,
                                       0,
                                       "");

    ByteBuffer buf = ByteBuffer.allocate(1024*1024*60);

    byte[] b = new byte[1024];
    while (config.read(b) != -1) {
      buf.put(b);
    }
    config.close();
    
    String conf = Base64.encode(buf.array(), 0, buf.array().length, 0,"");
    buf.clear();
    
    String sURL = BASE_URL + pathToWS + "/" + userName + "/" + pass + "/" + encodedPath + "/"
        + OperationType.RESTORE;
    return transport.execute(sURL);
  }

  /**
   * {@inheritDoc}
   */
  public String drop(String pathToWS) throws IOException, BackupExecuteException {
    // TODO Auto-generated method stub
    return "Command is unimplemented.";
  }

}

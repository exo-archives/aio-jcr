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
package org.exoplatform.services.ftp;

import java.io.File;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import org.apache.commons.chain.Catalog;
import org.apache.commons.logging.Log;
import org.exoplatform.services.command.impl.CommandService;
import org.exoplatform.services.ftp.client.FtpClientSession;
import org.exoplatform.services.ftp.client.FtpClientSessionImpl;
import org.exoplatform.services.ftp.command.FtpCommand;
import org.exoplatform.services.ftp.config.FtpConfig;
import org.exoplatform.services.ftp.data.FtpDataChannelManager;
import org.exoplatform.services.ftp.data.FtpDataChannelManagerImpl;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SAS Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * 
 * @version $Id: $
 */

public class FtpServerImpl implements FtpServer {

  private static Log                  log          = ExoLogger.getLogger(FtpConst.FTP_PREFIX
                                                       + "FtpServerImpl");

  public static final String          COMMAND_PATH = "/conf/ftp-commands.xml";

  private Catalog                     commandCatalog;

  private ManageableRepository        repository;

  private FtpConfig                   configuration;

  private FtpAcceptThread             acceptThread;

  private FtpDataChannelManager       dataChannelManager;

  private ArrayList<FtpClientSession> clients      = new ArrayList<FtpClientSession>();

  public FtpServerImpl(FtpConfig configuration,
                       CommandService commandService,
                       ManageableRepository repository) throws Exception {
    this.configuration = configuration;
    this.repository = repository;

    InputStream commandStream = getClass().getResourceAsStream(COMMAND_PATH);

    commandService.putCatalog(commandStream);
    commandCatalog = commandService.getCatalog(FtpConst.FTP_COMMAND_CATALOG);
  }

  protected void prepareCache() {
    String cacheFolderName = configuration.getCacheFolderName();

    File cacheFolder = new File(cacheFolderName);

    if (!cacheFolder.exists()) {
      log.info("Cache folder not exist. Try to create it...");
      cacheFolder.mkdir();
    }

    String[] cacheFiles = cacheFolder.list();
    if (cacheFiles == null) {
      log.info("No cache file in cache folder!");
      return;
    }

    for (String cacheFile : cacheFiles) {
      if (cacheFile.endsWith(FtpConst.FTP_CACHEFILEEXTENTION)) {
        File file = new File(cacheFolderName + "/" + cacheFile);
        file.delete();
      }
    }

  }

  public boolean start() {
    try {
      prepareCache();

      ServerSocket serverSocket = new ServerSocket(configuration.getCommandPort());

      dataChannelManager = new FtpDataChannelManagerImpl(configuration);

      acceptThread = new FtpAcceptThread(this, serverSocket);
      acceptThread.start();

      return true;
    } catch (Exception exc) {
      log.info("Unhandled exception. " + exc.getMessage(), exc);
    }

    return false;
  }

  public boolean stop() {
    return false;
  }

  public FtpConfig getConfiguration() {
    return configuration;
  }

  public ManageableRepository getRepository() {
    return repository;
  }

  public FtpCommand getCommand(String commandName) {
    return (FtpCommand) commandCatalog.getCommand(commandName);
  }

  public FtpDataChannelManager getDataChannelManager() {
    return dataChannelManager;
  }

  public boolean unRegisterClient(FtpClientSession clientSession) {
    boolean result = clients.remove(clientSession);
    log.info(">>> Client disconnected. Clients: " + clients.size());
    return result;
  }

  public int getClientsCount() {
    return clients.size();
  }

  protected class FtpAcceptThread extends Thread {

    protected FtpServer    ftpServer;

    protected ServerSocket serverSocket;

    protected boolean      enable = true;

    public FtpAcceptThread(FtpServer ftpServer, ServerSocket serverSocket) {
      this.ftpServer = ftpServer;
      this.serverSocket = serverSocket;
    }

    public void disable() {
      enable = false;
    }

    public void run() {
      while (enable) {
        Socket incoming = null;
        try {
          incoming = serverSocket.accept();
          FtpClientSession clientSession = new FtpClientSessionImpl(ftpServer, incoming);
          clients.add(clientSession);

          log.info(">>> New client connected. Clients: " + clients.size());
        } catch (Exception exc) {
          log.info("Unhandled exception. " + exc.getMessage(), exc);
        }
      }
    }

  }

}

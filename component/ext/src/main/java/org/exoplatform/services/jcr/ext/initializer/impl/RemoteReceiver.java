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
package org.exoplatform.services.jcr.ext.initializer.impl;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.CountDownLatch;

import org.apache.commons.logging.Log;
import org.exoplatform.services.jcr.ext.initializer.RemoteWorkspaceInitializationException;
import org.exoplatform.services.jcr.ext.replication.async.IncomeDataContext;
import org.exoplatform.services.jcr.ext.replication.async.RemoteExportException;
import org.exoplatform.services.jcr.ext.replication.async.storage.Member;
import org.exoplatform.services.jcr.ext.replication.async.storage.RandomChangesFile;
import org.exoplatform.services.jcr.ext.replication.async.storage.ResourcesHolder;
import org.exoplatform.services.jcr.ext.replication.async.transport.AbstractPacket;
import org.exoplatform.services.jcr.ext.replication.async.transport.AsyncPacketListener;
import org.exoplatform.services.jcr.ext.replication.async.transport.AsyncPacketTypes;
import org.exoplatform.services.jcr.ext.replication.async.transport.ErrorPacket;
import org.exoplatform.services.jcr.ext.replication.async.transport.MemberAddress;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SAS.
 * 
 * <br/>Date: 17.03.2009
 * 
 * @author <a href="mailto:alex.reshetnyak@exoplatform.com.ua">Alex Reshetnyak</a>
 * @version $Id: RemoteReceiver.java 111 2008-11-11 11:11:11Z rainf0x $
 */
public class RemoteReceiver implements AsyncPacketListener {
  
  /**
   * The apache logger.
   */
  private static Log              log = ExoLogger.getLogger("ext.RemoteReceiver");
  
  /**
   * The temporary folder.
   */
  private final File                             tempDir;

  private IncomeDataContext                      context;

  private CountDownLatch                         latch;

  private RemoteWorkspaceInitializationException exception = null;

  /**
   * RemoteReceiver constructor.
   * 
   * @param tempDir
   *          the temporary folder
   */
  public RemoteReceiver(File tempDir) {
    this.tempDir = tempDir;
  }

  /**
   * {@inheritDoc}
   */
  public void onError(MemberAddress sourceAddress) {
    // TODO Auto-generated method stub

  }

  /**
   * {@inheritDoc}
   */
  public void receive(AbstractPacket packet, MemberAddress sourceAddress) {
    switch (packet.getType()) {
    case WorkspaceDataPacket.WORKSPACE_DATA_PACKET: {
      try {
        WorkspaceDataPacket wdPacket = (WorkspaceDataPacket) packet;
        // get associated changes file
        if (context == null) {
          RandomChangesFile changesFile;
          try {
            File subDir = new File(tempDir.getCanonicalPath() + File.separator
                + System.currentTimeMillis());
            subDir.mkdirs();

            File wdFile = File.createTempFile("wdFile", "-" + System.currentTimeMillis(), subDir);

            changesFile = new RandomChangesFile(wdFile, wdPacket.getCRC(), 1, new ResourcesHolder());
          } catch (NoSuchAlgorithmException e) {
            throw new IOException(e.getMessage());
          }

          context = new IncomeDataContext(changesFile,
                                          new Member(sourceAddress, -1),
                                          wdPacket.getPacketsCount());

        }

        context.writeData(wdPacket.getBuffer(), wdPacket.getOffset());

        if (context.isFinished()) {
          latch.countDown();
        }

      } catch (IOException e) {
        log.error("Cannot save workspace data changes", e);
        exception = new RemoteWorkspaceInitializationException("Cannot save workspace data changes",
                                                               e);
        latch.countDown();
      }
    }
      break;

    case AsyncPacketTypes.EXPORT_ERROR: {
      ErrorPacket ePacket = (ErrorPacket) packet;
      exception = new RemoteWorkspaceInitializationException(ePacket.getErrorMessage());
      latch.countDown();
    }
      break;
    }

  }

}

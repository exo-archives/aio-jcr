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

import org.exoplatform.services.log.Log;
import org.exoplatform.services.jcr.ext.initializer.RemoteWorkspaceInitializationException;
import org.exoplatform.services.jcr.ext.replication.storage.IncomeDataContext;
import org.exoplatform.services.jcr.ext.replication.storage.Member;
import org.exoplatform.services.jcr.ext.replication.storage.RandomChangesFile;
import org.exoplatform.services.jcr.ext.replication.storage.ResourcesHolder;
import org.exoplatform.services.jcr.ext.replication.transport.AbstractPacket;
import org.exoplatform.services.jcr.ext.replication.transport.MemberAddress;
import org.exoplatform.services.jcr.ext.replication.transport.PacketListener;
import org.exoplatform.services.jcr.ext.replication.transport.StateEvent;
import org.exoplatform.services.jcr.ext.replication.transport.StateListener;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SAS.
 * 
 * <br/>
 * Date: 17.03.2009
 * 
 * @author <a href="mailto:alex.reshetnyak@exoplatform.com.ua">Alex Reshetnyak</a>
 * @version $Id: RemoteReceiver.java 111 2008-11-11 11:11:11Z rainf0x $
 */
public class RemoteReceiver implements PacketListener, StateListener {

  /**
   * The apache logger.
   */
  private static Log                             log           = ExoLogger.getLogger("ext.RemoteReceiver");

  /**
   * The temporary folder.
   */
  private final File                             tempDir;

  /**
   * Input data context.
   */
  private IncomeDataContext                      context;

  /**
   * The latch.
   */
  private CountDownLatch                         latch;

  /**
   * The saved exception.
   */
  private RemoteWorkspaceInitializationException exception     = null;

  /**
   * The members count.
   */
  private int                                    membrerInited = 0;

  /**
   * RemoteReceiver constructor.
   * 
   * @param tempDir
   *          the temporary folder
   * @param latch
   *          the synchronization latch
   */
  public RemoteReceiver(File tempDir, CountDownLatch latch) {
    this.tempDir = tempDir;
    this.latch = latch;
  }

  /**
   * {@inheritDoc}
   */
  public void onError(MemberAddress sourceAddress) {
    // do not use.
  }

  /**
   * {@inheritDoc}
   */
  public void receive(AbstractPacket packet, MemberAddress sourceAddress) {
    switch (packet.getType()) {
    case WorkspaceDataPacket.WORKSPACE_DATA_PACKET:
      try {
        WorkspaceDataPacket wdPacket = (WorkspaceDataPacket) packet;
        // get associated changes file
        if (context == null) {
          RandomChangesFile changesFile;
          try {
            File subDir = new File(tempDir.getCanonicalPath() + File.separator
                + System.currentTimeMillis());
            subDir.mkdirs();

            File wdFile = File.createTempFile("wdFile", ".0", subDir);

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
      break;

    case InitializationErrorPacket.INITIALIZATION_ERROR_PACKET:
      InitializationErrorPacket ePacket = (InitializationErrorPacket) packet;
      exception = new RemoteWorkspaceInitializationException(ePacket.getErrorMessage());
      latch.countDown();
      break;

    default:
      break;
    }

  }

  /**
   * getContext.
   * 
   * @return IncomeDataContext the input data contex
   */
  public IncomeDataContext getContext() {
    return context;
  }

  /**
   * getException.
   * 
   * @return RemoteWorkspaceInitializationException the saved exception
   */
  public RemoteWorkspaceInitializationException getException() {
    return exception;
  }

  /**
   * {@inheritDoc}
   */
  public void onStateChanged(StateEvent event) {
    if (membrerInited == 2 && event.getMembers().size() == 1
        && (context == null || !context.isFinished())) {
      exception = new RemoteWorkspaceInitializationException("The remote member was disconected");
      latch.countDown();
    } else
      membrerInited = event.getMembers().size();
  }

}

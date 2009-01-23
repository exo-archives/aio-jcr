/**
 * 
 */
/*
 * Copyright (C) 2003-2008 eXo Platform SAS.
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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.jcr.RepositoryException;

import org.apache.commons.logging.Log;
import org.exoplatform.services.jcr.core.nodetype.NodeTypeDataManager;
import org.exoplatform.services.jcr.dataflow.DataManager;
import org.exoplatform.services.jcr.datamodel.NodeData;
import org.exoplatform.services.jcr.ext.replication.async.storage.ChangesFile;
import org.exoplatform.services.jcr.ext.replication.async.storage.Member;
import org.exoplatform.services.jcr.ext.replication.async.transport.MemberAddress;
import org.exoplatform.services.jcr.impl.Constants;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SAS.
 * 
 * <br/>Handles request on remote export.
 * 
 * <br/>Date: 12.12.2008
 * 
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a>
 * @version $Id$
 */
public class RemoteExportServerImpl implements RemoteExportServer, LocalEventListener,
    RemoteEventListener {

  protected static final Log          LOG     = ExoLogger.getLogger("jcr.RemoteExportServerImpl");

  protected final AsyncTransmitter    transmitter;

  protected final DataManager         dataManager;

  protected final NodeTypeDataManager ntManager;

  protected final Set<ExportWorker>   workers = new LinkedHashSet<ExportWorker>();

  protected boolean                   stopped = false;

  class ExportWorker extends Thread {
    final MemberAddress member;

    final String nodeId;

    ExportWorker(MemberAddress member, String nodeId) {
      this.member = member;
      this.nodeId = nodeId;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void run() {
      try {
        ChangesFile chl = getExportChanges(nodeId);
        transmitter.sendExport(chl, member);

        if (LOG.isDebugEnabled())
          LOG.debug("Remote export request served, send result to member " + member);
      } catch (IOException e) {
        LOG.error("IO error on send export changes " + e, e);
      } catch (RepositoryException e) {
        LOG.error("Repository error on remote export request " + e, e);
        try {
          transmitter.sendError("error " + e, member);
        } catch (IOException ioe) {
          LOG.error("IO error on send error message " + e, e);
        }
      } catch (RemoteExportException e) {
        LOG.error("Remote export request causes the error " + e, e);
        try {
          transmitter.sendError("error " + e, member);
        } catch (IOException ioe) {
          LOG.error("IO error on send error message " + e, e);
        }
      } finally {
        workers.remove(this);
      }
    }
  }

  public RemoteExportServerImpl(AsyncTransmitter transmitter,
                                DataManager dataManager,
                                NodeTypeDataManager ntManager) {
    this.transmitter = transmitter;
    this.dataManager = dataManager;
    this.ntManager = ntManager;
  }

  /**
   * Return Node traversed changes log for a given path.<br/> Used by receiver.
   * 
   * @param nodeId
   *          Node QPath
   * @return ChangesLogFile
   * @throws RemoteExportException
   *           if IO error occurs
   * @throws RepositoryException
   *           if Repository error occurs
   */
  protected ChangesFile getExportChanges(String nodeId) throws RepositoryException,
                                                       RemoteExportException {

    NodeData exportedNode = (NodeData) dataManager.getItemData(nodeId);
    NodeData parentNode;
    if (nodeId.equals(Constants.ROOT_UUID)) {
      parentNode = exportedNode;
    } else {
      parentNode = (NodeData) dataManager.getItemData(exportedNode.getParentIdentifier());
    }

    ObjectOutputStream out = null;
    try {
      File chLogFile = File.createTempFile(ChangesFile.PREFIX, ChangesFile.SUFIX);
      MessageDigest digest;
      try {
        digest = MessageDigest.getInstance("MD5");
      } catch (NoSuchAlgorithmException e) {
        throw new RemoteExportException(e);
      }

      DigestOutputStream dout = new DigestOutputStream(new FileOutputStream(chLogFile), digest);
      out = new ObjectOutputStream(dout);

      // extract ItemStates
      ItemDataExportVisitor exporter = new ItemDataExportVisitor(out,
                                                                 parentNode,
                                                                 ntManager,
                                                                 dataManager);
      exportedNode.accept(exporter);

      String crc = new String(digest.digest(), Constants.DEFAULT_ENCODING);
      return new ChangesFile(chLogFile, crc, System.currentTimeMillis());
    } catch (IOException e) {
      throw new RemoteExportException(e);
    } finally {
      if (out != null)
        try {
          out.close();
        } catch (IOException e) {
          LOG.error("I/O error on result stream close " + e, e);
        }
    }
  }

  /**
   * {@inheritDoc}
   */
  public void sendExport(RemoteExportRequest event) {
    if (this.stopped) {
      LOG.warn("Export server stopped. Cannot handle SEND EXPORT request for Node Id "
          + event.getNodeId() + ". Request from " + event.getMember());
    } else {
      ExportWorker export = new ExportWorker(event.getMember(), event.getNodeId());
      export.start();

      workers.add(export);
    }
  }

  /**
   * {@inheritDoc}
   */
  public void onCancel() {
    for (ExportWorker worker : workers) {
      worker.interrupt();
      LOG.info("Interrupt export for member " + worker.member);
    }
  }

  /**
   * {@inheritDoc}
   */
  public void onDisconnectMembers(List<Member> members) {
    for (ExportWorker worker : workers) {
      if (members.contains(worker.member)) {
        worker.interrupt();
        LOG.info("Interrupt export for member " + worker.member);
      }
    }
  }

  /**
   * {@inheritDoc}
   */
  public void onStop() {
    // set flag
    this.stopped = true;
  }

  /**
   * {@inheritDoc}
   */
  public void onMerge(Member member) {
    // not interested
  }

  /**
   * {@inheritDoc}
   */
  public void onStart(Member localMember, List<Member> members) {
    // not interested
  }

}

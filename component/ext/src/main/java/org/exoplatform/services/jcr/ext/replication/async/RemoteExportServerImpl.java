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
import org.exoplatform.services.jcr.ext.replication.async.storage.ResourcesHolder;
import org.exoplatform.services.jcr.ext.replication.async.storage.SimpleChangesFile;
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

  protected static final Log          LOG         = ExoLogger.getLogger("jcr.RemoteExportServerImpl");

  public static final String          FILE_PREFIX = "export";

  protected final AsyncTransmitter    transmitter;

  protected final DataManager         dataManager;

  protected final NodeTypeDataManager ntManager;

  protected final Set<ExportWorker>   workers     = new LinkedHashSet<ExportWorker>();

  protected final ResourcesHolder     resHolder   = new ResourcesHolder();

  protected boolean                   stopped     = false;

  class ExportWorker extends Thread {
    final MemberAddress member;

    final String        nodeId;

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
        LOG.info("Remote EXPORT request from member " + member + ", node " + nodeId);
        
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
      } catch(Throwable e){
        LOG.error("Exception on remote export request " + e, e);
        try {
          transmitter.sendError("error " + e, member);
        } catch (IOException ioe) {
          LOG.error("IO error on send error message " + e, e);
        }
      }
      
      finally {
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
      // TODO make it simplier
      File chLogFile = File.createTempFile(FILE_PREFIX, "-" + nodeId);
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

      byte[] crc = digest.digest();
      return new SimpleChangesFile(chLogFile, crc, System.currentTimeMillis(), resHolder);
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
    if (LOG.isDebugEnabled())
      LOG.debug("On CANCEL");
    
    for (ExportWorker worker : workers) {
      try {
        worker.join();
      } catch (InterruptedException e) {
        LOG.error("Cancel error " + e);
      }
      LOG.info("Cancel export for member " + worker.member);
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

    if (LOG.isDebugEnabled())
      LOG.debug("On STOP");
      
    try {
      this.resHolder.close();
    } catch (IOException e) {
      LOG.error("Error of data streams close " + e, e);
    }

    // set flag
    this.stopped = true;
  }

  /**
   * {@inheritDoc}
   */
  public void onMerge(MemberAddress member) {
    // not interested
  }

  /**
   * {@inheritDoc}
   */
  public void onStart(List<MemberAddress> members) {
    // not interested
  }

}

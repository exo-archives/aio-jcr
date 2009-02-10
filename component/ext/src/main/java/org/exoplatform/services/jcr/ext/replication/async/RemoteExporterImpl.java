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
import java.io.IOException;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.CountDownLatch;

import org.apache.commons.logging.Log;
import org.exoplatform.services.jcr.dataflow.ItemState;
import org.exoplatform.services.jcr.ext.replication.async.storage.ChangesStorage;
import org.exoplatform.services.jcr.ext.replication.async.storage.ItemStatesStorage;
import org.exoplatform.services.jcr.ext.replication.async.storage.Member;
import org.exoplatform.services.jcr.ext.replication.async.storage.RandomChangesFile;
import org.exoplatform.services.jcr.ext.replication.async.storage.ResourcesHolder;
import org.exoplatform.services.jcr.ext.replication.async.transport.MemberAddress;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SAS. <br/>Date: 11.12.2008
 * 
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a>
 * @version $Id$
 */
public class RemoteExporterImpl implements RemoteExporter, RemoteExportClient {

  public static final String       FILE_PREFIX  = "exporter";

  /**
   * Logger.
   */
  private static final Log         LOG          = ExoLogger.getLogger("ext.RemoteExporterImpl");

  protected final AsyncTransmitter transmitter;

  protected final AsyncReceiver    receiver;

  protected final ResourcesHolder  resHolder    = new ResourcesHolder();

  protected final File             tempDir;

  /**
   * Member address. Mutable value. Will be changed by Merge manager on each members pair merge.
   */
  protected MemberAddress          remoteMember;

  /**
   * Current changesFile owner.
   */
  protected Member                 changesOwner = null;

  /**
   * Changes file.
   */
  private RandomChangesFile        changesFile  = null;

  private CountDownLatch           latch;

  private RemoteExportException    exception    = null;

  RemoteExporterImpl(AsyncTransmitter transmitter, AsyncReceiver receiver, String tempDir) {
    this.transmitter = transmitter;
    this.receiver = receiver;
    this.tempDir = new File(tempDir);
  }

  /**
   * {@inheritDoc}
   */
  public ChangesStorage<ItemState> exportItem(String nodeId) throws RemoteExportException {
    
    LOG.info("Remote EXPORT from member " + remoteMember + ", node " + nodeId);
    
    receiver.setRemoteExportListener(this);

    // send request
    try {
      transmitter.sendGetExport(nodeId, remoteMember);
    } catch (IOException e) {
      throw new RemoteExportException(e);
    }

    latch = new CountDownLatch(1);
    try {
      latch.await();
    } catch (InterruptedException e) {
      throw new RemoteExportException(e);
    } finally {
      receiver.removeRemoteExportListener();
      // Throw internal exceptions
      if (exception != null)
        try {
          throw new RemoteExportException(exception);
        } finally {
          exception = null;
        }
    }

    LOG.info("Remote EXPORT DONE from member " + remoteMember + ", node " + nodeId);
    
    // check checksums
    try {
      DigestInputStream dis = new DigestInputStream(changesFile.getInputStream(),
                                                    MessageDigest.getInstance("MD5"));

      try {
        byte[] buf = new byte[1024];
        int len;
        while ((len = dis.read(buf)) > 0) {
        }

        if (!MessageDigest.isEqual(dis.getMessageDigest().digest(),
                                   changesFile.getChecksum())) {
          // TODO checksum don't work. fix it
          // throw new RemoteExportException("Remote export failed. Received data corrupted.");
        }

      } finally {
        dis.close();
      }

      if (changesOwner == null)
        throw new RemoteExportException("Changes owner (member) is not set");

      // return Iterator based on ChangesFile
      return new ItemStatesStorage<ItemState>(changesFile, changesOwner);
    } catch (IOException e) {
      throw new RemoteExportException(e);
    } catch (NoSuchAlgorithmException e) {
      throw new RemoteExportException(e);
    } finally {
      changesFile = null; // TODO
    }
  }

  /**
   * {@inheritDoc}
   */
  public void setRemoteMember(MemberAddress address) {
    remoteMember = address;
  }

  /**
   * {@inheritDoc}
   */
  public void onRemoteExport(RemoteExportResponce event) {
    try {
      switch (event.getType()) {
      case RemoteExportResponce.FIRST:
        initChangesFile(event.getCRC(), event.getTimeStamp());
        changesOwner = event.getMember();
        changesFile.writeData(event.getBuffer(), event.getOffset());
        break;

      case RemoteExportResponce.MIDDLE:
        initChangesFile(event.getCRC(), event.getTimeStamp());
        changesFile.writeData(event.getBuffer(), event.getOffset());
        break;

      case RemoteExportResponce.LAST:
        changesFile.finishWrite();
        
        //check received file
        //MessageDigest digest;
        //try {
        //  digest = MessageDigest.getInstance("MD5");
        //} catch (NoSuchAlgorithmException e) {
        //  throw new IOException(e.getMessage());
        //}
        //DigestInputStream in = new DigestInputStream(changesFile.getInputStream(),digest);
        
        //byte[] buf  = new byte[1024];
        //while(in.read(buf)!=-1);
        
        //String d = new String(digest.digest(),"UTF-8");
        
        changesFile.validate();
        
        latch.countDown();
        
        break;
      }
    } catch (IOException e) {
      LOG.error("Cannot save export changes", e);
      exception = new RemoteExportException(e);
      latch.countDown();
    }
  }

  /**
   * {@inheritDoc}
   */
  public void onRemoteError(RemoteExportError event) {

    // TODO delete ChangesFile

    // log exception
    exception = new RemoteExportException(event.getErrorMessage());
    latch.countDown();
  }

  private void initChangesFile(byte[] crc, long timeStamp) throws IOException {
    if (this.changesFile == null) {
      
      try {
        changesFile = new RandomChangesFile(File.createTempFile(FILE_PREFIX, "-" + timeStamp),
                                            crc,
                                            timeStamp,
                                            resHolder);
      } catch (NoSuchAlgorithmException e) {
        throw new IOException (e.getMessage());
      }
      
    }
  }

  /**
   * {@inheritDoc}
   */
  public void cleanup() {
    try {
      resHolder.close();
    } catch (IOException e) {
      LOG.error("Error of data fiels close " + e, e);
    }

    for (File f : tempDir.listFiles())
      if (!f.delete())
        LOG.warn("Cannot delete exporter temp file " + f.getAbsolutePath());
  }
}

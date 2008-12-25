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
import java.io.RandomAccessFile;
import java.util.Iterator;
import java.util.concurrent.CountDownLatch;

import org.apache.commons.logging.Log;
import org.exoplatform.services.jcr.dataflow.ItemState;
import org.exoplatform.services.jcr.ext.replication.async.storage.ChangesFile;
import org.exoplatform.services.jcr.ext.replication.async.storage.ItemStateIterator;
import org.exoplatform.services.jcr.ext.replication.async.transport.Member;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SAS.
 * 
 * <br/>Date: 11.12.2008
 * 
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a>
 * @version $Id$
 */
public class RemoteExporterImpl implements RemoteExporter, RemoteExportClient {

  /**
   * log. the apache logger.
   */
  private static Log               log = ExoLogger.getLogger("ext.RemoteExporterImpl");

  protected final AsyncTransmitter transmitter;

  protected final AsyncReceiver    receiver;

  /**
   * Member address. Mutable value. Will be changed by Merge manager on each memebers pair merge.
   */
  protected Member                 memberAddress;

  private File                     storageFile;

  private CountDownLatch           latch;

  private RandomAccessFile         rendomAccessStorageFile;

  RemoteExporterImpl(AsyncTransmitter transmitter, AsyncReceiver receiver) {
    this.transmitter = transmitter;
    this.receiver = receiver;
  }

  /**
   * {@inheritDoc}
   */
  public Iterator<ItemState> exportItem(String nodetId) throws IOException {
    // registration RemoteChangesListener.
    receiver.setRemoteExportListener(this);

    // send request
    transmitter.sendGetExport(nodetId, memberAddress);
    
    latch = new CountDownLatch(1);
    try {
       latch.wait();
    } catch (InterruptedException e) {
      //TODO
    }

    receiver.removeRemoteExportListener();

    // TODO make checksum
    ChangesFile changesFile = new ChangesFile(storageFile, "TODO", 0);

    ItemStateIterator<ItemState> satesIterator = new ItemStateIterator<ItemState>(changesFile);

    return satesIterator;
  }

  /**
   * {@inheritDoc}
   */
  public void setMemberAddress(Member address) {
    memberAddress = address;
  }

  /**
   * {@inheritDoc}
   */
  public void onRemoteExport(RemoteExportResponce event) {
    try {
      switch (event.getType()) {
      case RemoteExportResponce.FIRST:
        storageFile = File.createTempFile("romoteExport", "tmp");
        rendomAccessStorageFile = new RandomAccessFile(storageFile, "w");

        rendomAccessStorageFile.write(event.getBuffer(),
                                      (int) event.getOffset(),
                                      event.getBuffer().length);
        break;

      case RemoteExportResponce.MIDDLE:
        rendomAccessStorageFile.write(event.getBuffer(),
                                      (int) event.getOffset(),
                                      event.getBuffer().length);
        break;

      case RemoteExportResponce.LAST:
        rendomAccessStorageFile.close();
        latch.countDown();
        break;

      }
    } catch (IOException e) {
      // TODO
      log.error("Cannot save export changes", e);
    }
  }

  /**
   * {@inheritDoc}
   */
  public void onRemoteError(RemoteExportError event) {
    // TODO
    // - delete export file
    // - stop waiting in exportItem() method
  }

}

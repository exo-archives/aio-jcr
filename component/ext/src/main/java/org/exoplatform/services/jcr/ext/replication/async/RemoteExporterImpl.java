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

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.concurrent.CountDownLatch;

import org.apache.commons.logging.Log;
import org.exoplatform.services.jcr.dataflow.ItemState;
import org.exoplatform.services.jcr.ext.replication.async.storage.ChangesFile;
import org.exoplatform.services.jcr.ext.replication.async.transport.Member;
import org.exoplatform.services.jcr.impl.Constants;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SAS. <br/>Date: 11.12.2008
 * 
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a>
 * @version $Id$
 */
public class RemoteExporterImpl implements RemoteExporter, RemoteExportClient {

  /**
   * Logger.
   */
  private static Log               log         = ExoLogger.getLogger("ext.RemoteExporterImpl");

  protected final AsyncTransmitter transmitter;

  protected final AsyncReceiver    receiver;

  /**
   * Member address. Mutable value. Will be changed by Merge manager on each members pair merge.
   */
  protected Member                 memberAddress;

  /**
   * Changes file.
   */
  private ChangesFile              changesFile = null;

  private CountDownLatch           latch;

  private RemoteExportException    exception   = null;

  class ResultIterator<T extends ItemState> implements Iterator<T> {
    private T                 nextItem;

    private ObjectInputStream in;

    public ResultIterator() throws RemoteExportException {
      try {
        this.in = new ObjectInputStream(changesFile.getDataStream());
        this.nextItem = readNext();
      } catch (IOException e) {
        throw new RemoteExportException(e);
      } catch (ClassNotFoundException e) {
        throw new RemoteExportException(e);
      } catch (ClassCastException e) {
        throw new RemoteExportException(e);
      }
    }

    public boolean hasNext() {
      return nextItem != null;
    }

    public T next() throws NoSuchElementException {
      if (nextItem == null)
        throw new NoSuchElementException();

      T retVal = nextItem;
      try {
        nextItem = readNext();
      } catch (IOException e) {
        throw new NoSuchElementException(e.getMessage());
      } catch (ClassNotFoundException e) {
        throw new NoSuchElementException(e.getMessage());
      } catch (ClassCastException e) {
        throw new NoSuchElementException(e.getMessage());
      }

      return retVal;
    }

    public void remove() {
      // TODO Auto-generated method stub
    }

    @SuppressWarnings("unchecked")
    protected T readNext() throws IOException, ClassNotFoundException, ClassCastException {
      try {
        return (T) in.readObject();
      } catch (EOFException e) {
        // End of list
        return null;
      }
    }
  }

  RemoteExporterImpl(AsyncTransmitter transmitter, AsyncReceiver receiver) {
    this.transmitter = transmitter;
    this.receiver = receiver;
  }

  /**
   * {@inheritDoc}
   */
  public Iterator<ItemState> exportItem(String nodetId) throws RemoteExportException {
    // registration RemoteChangesListener.
    receiver.setRemoteExportListener(this);

    // send request
    transmitter.sendGetExport(nodetId, memberAddress);

    // ChangesFile changesFile = new ChangesFile("TODO", 0);

    latch = new CountDownLatch(1);
    try {
      latch.wait();
    } catch (InterruptedException e) {
      throw new RemoteExportException(e);
    } finally {
      receiver.removeRemoteExportListener();
      // Throw internal exceptions
      if (exception != null)
        try {
          throw exception;
        } finally {
          exception = null;
        }
    }

    // check checksums
    try {
      DigestInputStream dis = new DigestInputStream(changesFile.getDataStream(),
                                                    MessageDigest.getInstance("MD5"));
      byte[] buf = new byte[1024];
      int len;
      while ((len = dis.read(buf)) > 0) {
      }

      if (!MessageDigest.isEqual(dis.getMessageDigest().digest(),
                                 changesFile.getChecksum().getBytes(Constants.DEFAULT_ENCODING))) {

        throw new RemoteExportException("Remote export failed. Received data corrupted.");
      }
    } catch (IOException e) {
      throw new RemoteExportException(e);
    } catch (NoSuchAlgorithmException e) {
      throw new RemoteExportException(e);
    }

    // return Iterator based on ChangesFile
    return new ResultIterator<ItemState>();
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
        initChangesFile(event.getCRC(), event.getTimeStamp());
        changesFile.writeData(event.getBuffer(), event.getOffset());
        break;

      case RemoteExportResponce.MIDDLE:
        initChangesFile(event.getCRC(), event.getTimeStamp());
        changesFile.writeData(event.getBuffer(), event.getOffset());
        break;

      case RemoteExportResponce.LAST:
        if (changesFile != null)
          changesFile.finishWrite();
        latch.countDown();
        break;
      }
    } catch (IOException e) {
      log.error("Cannot save export changes", e);
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

  private void initChangesFile(String crc, long timeStamp) throws IOException {
    if (this.changesFile == null) {
      changesFile = new ChangesFile(crc, timeStamp);
    }
  }

}

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

package org.exoplatform.services.cifs.server.filesys;

import java.io.IOException;
import java.io.InputStream;

import javax.jcr.AccessDeniedException;
import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.apache.commons.logging.Log;
import org.exoplatform.services.jcr.core.value.EditableBinaryValue;
import org.exoplatform.services.log.ExoLogger;

/**
 * JCR representation of network file.
 * <p>
 * Created by The eXo Platform SAS Author : Karpenko Sergey
 */
public class JCRNetworkFile extends NetworkFile {
  private static final Log    logger       = ExoLogger
                                               .getLogger("org.exoplatform.services.cifs.smb.server.JCRNetworkFile");

  // Reference to node representing the file
  private Node                node;

  // BinareValue that used for random write in file;
  private EditableBinaryValue exv;

  // Flag that shows is value was edited
  private boolean             isAnyChanges = false;

  public JCRNetworkFile(Node n) {
    super();
    node = n;
  }

  public JCRNetworkFile() {
    super();
  }

  public void setNodeRef(Node n) {
    node = n;
  }

  public Node getNodeRef() {
    return node;
  }

  protected void assignExtendedBinaryValue() throws IOException, RepositoryException {
    if (!node.isNodeType("nt:file"))
      throw new AccessDeniedException();

    exv = (EditableBinaryValue) getNodeRef().getNode("jcr:content").getProperty("jcr:data")
        .getValue();
  }

  public boolean isExtendedBinaryValueAssigned() {
    return (exv != null) ? true : false;
  }

  public void updateFile(InputStream is, int datalength, long position) throws IOException,
      RepositoryException {

    if (!isExtendedBinaryValueAssigned())
      assignExtendedBinaryValue();

    exv.update(is, datalength, position);

    isAnyChanges = true;

    // command Close_file have timeout, so server have not time to finish save
    // changes of large file to jcr, so large file will be saved by WriteFile
    // command;
    /*
     * if (((long) (datalength + position) >= closeOnSize) && (closeOnSize !=
     * -1) && (closeOnSize > MAX_FILE_SIZE_FOR_RANDOM_WRITE)) { flush();
     * logger.debug("file data completly writed into jcr node TEMPORARY"); }
     */
  }

  /**
   * Read <code>length</code> data from file at <code>position</code> to
   * <code>buffer</code> form <code>offset</code>
   * 
   * @param buffer Buffer to return data to
   * @param offset Starting position in the return buffer
   * @param length Maximum size of data to return
   * @param position File position to read data
   * @return Size of read data
   * @throws IOException
   * @throws RepositoryException
   */

  public int read(byte[] buffer, int offset, int length, long position) throws IOException,
      RepositoryException {

    int readed = 0;
    if (!isExtendedBinaryValueAssigned()) {

      // use persisted data
      if (!node.isNodeType("nt:file"))
        throw new AccessDeniedException();

      // Read a block of data from the file

      InputStream is = node.getNode("jcr:content").getProperty("jcr:data").getStream();

      long skip_count = is.skip(position);
      if ((skip_count < position) || (skip_count == -1)) {
        readed = 0;
      } else {
        byte[] tmpbuf = new byte[length];
        readed = is.read(tmpbuf);
        if (readed != -1) {
          System.arraycopy(tmpbuf, 0, buffer, offset, readed);
        } else {
          // Read count of -1 indicates a read past the end of file
          readed = 0;
        }
      }

      is.close();

    } else {
      // use extended value data

      ExtByteArrayOutputStream bout = new ExtByteArrayOutputStream(length);

      // read data to output stream

      if (exv.getLength() < position) {
        readed = (int) exv.read(bout, length, position);
      } else {
        // End file
        readed = -1;
      }

      // copy data to buffer (originally it is smb packet)
      if (readed != -1) {
        System.arraycopy(bout.getBuf(), 0, buffer, offset, readed);
      } else {
        // Read count of -1 indicates a read past the end of file
        readed = 0;
      }
    }
    return readed;
  }

  public void truncateFile(long size) throws IOException, RepositoryException {
    if (!isExtendedBinaryValueAssigned()) {
      assignExtendedBinaryValue();
    }
    exv.setLength(size);
    isAnyChanges = true;
  }

  /**
   * Put changes to property, but not save to persistent area.
   */
  public void flush() throws RepositoryException {
    if (isExtendedBinaryValueAssigned()) {

      getNodeRef().getNode("jcr:content").getProperty("jcr:data").setValue(exv);

      exv = null; // free the reference to property value
    }
    // else: do nothing
  }

  /**
   * Save any changes to persistent area. If file havn't changed - do nothing.
   */
  public void saveChanges() throws RepositoryException {
    if (isChanged()) {

      flush();
      getNodeRef().save();

      // clear all flags;
      isAnyChanges = false;
      releaseResources();
    }
  }

  public long getLength() throws RepositoryException {
    if (isExtendedBinaryValueAssigned() && isChanged()) {
      return exv.getLength();
    }

    return getNodeRef().getNode("jcr:content").getProperty("jcr:data").getLength();
  }

  public boolean isChanged() {
    return isAnyChanges;
  }

  /**
   * Release any resources associated with file without save. Also usable as
   * CANCLE.
   */
  public void releaseResources() {
    exv = null;

    // clear all flags;
    isAnyChanges = false;
  }

}

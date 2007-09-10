/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SAS          All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.cifs.server.filesys;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import javax.jcr.AccessDeniedException;
import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.apache.commons.logging.Log;
import org.exoplatform.services.cifs.server.filesys.NetworkFile;
import org.exoplatform.services.jcr.core.ExtendedProperty;
import org.exoplatform.services.jcr.core.value.EditableBinaryValue;
//import org.exoplatform.services.jcr.core.value.ExtendedBinaryValue;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SARL Author : Karpenko
 * 
 * TODO this class is potentaly unstable
 */

public class JCRNetworkFile extends NetworkFile {
  private static final Log logger = ExoLogger
      .getLogger("org.exoplatform.services.cifs.smb.server.JCRNetworkFile");

  private static int id = 0;

  // Reference to node represents the file
  private Node node;

  // BinareValue that used for random write in file;
  private EditableBinaryValue exv;

  // Stream used for read data from persistent storage
  // InputStream is;

  // byte count already read
// private long readcount = 0;

  private boolean isAnyChanges = false;

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

  public EditableBinaryValue getExtendedBinaryValue() {
    return exv;
  }

  protected void assignExtendedBinaryValue() throws IOException,
      RepositoryException {
    if (!node.isNodeType("nt:file"))
      throw new AccessDeniedException();
    
    exv = (EditableBinaryValue) getNodeRef().getNode("jcr:content")
        .getProperty("jcr:data").getValue();

    /*
     * if (is != null) { is.close(); is = null; readcount = 0; }
     */

  }

  public boolean isExtendedBinaryValueAssigned() {
    return (exv != null) ? true : false;
  }

  public void updateFile(InputStream is, int datalength, long position)
      throws IOException, RepositoryException {

    if (!isExtendedBinaryValueAssigned())
      assignExtendedBinaryValue();

    exv.update(is, datalength, position);

    isAnyChanges = true;
  }

  /**
   * Read <code>length</code> data from file at <code>position</code> to
   * <code>buffer</code> form <code>offset</code>
   * 
   * @param buffer
   *          Buffer to return data to
   * @param offset
   *          Starting position in the return buffer
   * @param length
   *          Maximum size of data to return
   * @param position
   *          File position to read data
   * @return Size of read data
   * @throws IOException
   * @throws RepositoryException
   */

  public int read(byte[] buffer, int offset, int length, long position)
      throws IOException, RepositoryException {

    int readed = 0;
    if (!isExtendedBinaryValueAssigned()) {
      // use persisted data

      if (!node.isNodeType("nt:file"))
        throw new AccessDeniedException();

      // Read a block of data from the file

      InputStream is = node.getNode("jcr:content").getProperty("jcr:data")
          .getStream();

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

/*
 * // check is file already read if (is == null) { is =
 * node.getNode("jcr:content").getProperty("jcr:data") .getStream(); position =
 * 0; }
 * 
 * if (position < readcount) { is.reset(); is.skip(position); }
 * 
 * if (position > readcount) { long skipcount = is.skip(position - readcount);
 * if ((skipcount + readcount < position) || (skipcount == -1)) return 0;
 * readcount += skipcount; }
 * 
 * byte[] tmpbuf = new byte[length]; readed = is.read(tmpbuf); if (readed != -1) {
 * System.arraycopy(tmpbuf, 0, buffer, offset, readed); readcount += readed; }
 * else { // Read count of -1 indicates a read past the end of file readed = 0; }
 */
    } else {
      // use extended value data

      ExtByteArrayOutputStream bout = new ExtByteArrayOutputStream(length);

      // read data to output stream
      readed = (int) exv.read(bout, length, position);

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
    if (!isExtendedBinaryValueAssigned())
      assignExtendedBinaryValue();

    exv.setLength(size);

    isAnyChanges = true;
  }

  /*
   * Put changes to property, but not save to persistent area.
   * 
   */
  public void flush() throws RepositoryException, IOException {
    if (isExtendedBinaryValueAssigned()) {
      getNodeRef().getNode("jcr:content").getProperty("jcr:data").setValue(exv);

      exv = null; // free the reference to property value
    }
    // else: do nothing
  }

  /*
   * save any changes to persistant area
   * 
   */
  public void saveChanges() throws IOException, RepositoryException {
    if (isChanged()) {
      flush();
      getNodeRef().save();
    }

  }

  public long getLength() throws RepositoryException {
    if (isExtendedBinaryValueAssigned() && isChanged()) {
      return exv.getLength();
    }

    return getNodeRef().getNode("jcr:content").getProperty("jcr:data")
        .getLength();
  }

  public boolean isChanged() {
    return isAnyChanges;
  }

}

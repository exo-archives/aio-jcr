/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SAS          All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.cifs.server.filesys;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.apache.commons.logging.Log;
import org.exoplatform.services.cifs.server.filesys.NetworkFile;
import org.exoplatform.services.jcr.core.ExtendedProperty;
import org.exoplatform.services.jcr.core.value.ExtendedBinaryValue;
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
  private ExtendedBinaryValue exv;

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

  public ExtendedBinaryValue getExtendedBinaryValue() {
    return exv;
  }

  protected void assignExtendedBinaryValue() throws RepositoryException {
    exv = (ExtendedBinaryValue) getNodeRef().getNode("jcr:content")
        .getProperty("jcr:data").getValue();

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

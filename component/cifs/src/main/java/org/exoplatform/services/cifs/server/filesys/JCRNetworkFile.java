/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SAS          All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.cifs.server.filesys;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import javax.jcr.Node;

import org.apache.commons.logging.Log;
import org.exoplatform.services.cifs.server.filesys.NetworkFile;
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

  private Node node;

  private FileChannel wrchannel;

  private boolean truncfirst = false;

  private File tmpfile;

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

  private void createTemporaryFileChannel() throws Exception {
    id++;
    String suf = String.valueOf(getFileId());
    tmpfile = File.createTempFile("tf_" + suf + id, ".tmp");
    tmpfile.deleteOnExit();

    wrchannel = new FileOutputStream(tmpfile, true).getChannel();
  }

  public long writeFile(byte[] buf, int dataPos, int dataLen, long offset)
      throws Exception {
    if (wrchannel == null)
      createTemporaryFileChannel();
    ByteBuffer byteBuffer = ByteBuffer.wrap(buf, dataPos, dataLen);
    int i = wrchannel.write(byteBuffer, offset);

    if (((long) (dataLen + offset) >= m_fileSize) && (m_fileSize != 0)) {
      FileInputStream fis = new FileInputStream(tmpfile);
      node.getNode("jcr:content").getProperty("jcr:data").setValue(fis);
      fis.close();
      wrchannel.close();
      tmpfile.delete();
      node.save();

      logger.debug("file data cmpletly writed into jcr node TEMPORARY");
    }

    return i;
  }

  public void truncateFile(long siz) throws IOException {
    try {
      if (wrchannel == null) {
        createTemporaryFileChannel();
        truncfirst = true;
      }

      wrchannel.truncate(siz);
      m_fileSize = siz;

      if (truncfirst == false) {
        FileInputStream fis = new FileInputStream(tmpfile);
        node.getNode("jcr:content").getProperty("jcr:data").setValue(fis);
        fis.close();
        wrchannel.close();
        tmpfile.delete();
        node.save();
        logger.debug("file data cmpletly writed into jcr node TEMPORARY");
        return;
      }
      logger.debug("file truncated for " + siz + " bytes TEMPORARY");
    } catch (Exception e) {
      throw new IOException(e.getMessage());
    }
    
  }
  public void flushFile() throws Exception {
    try {
      if (tmpfile != null) {
        FileInputStream st = new FileInputStream(tmpfile);

        if (node != null) {
          node.getNode("jcr:content").getProperty("jcr:data").setValue(st);
          node.save();
        }
        st.close();

        tmpfile.delete();
        tmpfile = null;
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    
  }
  /**
   * This metod is called whan NetworkFile is destroys
   */
  public void remove() {
    try {
      if (wrchannel != null)
        wrchannel.close();
      if (tmpfile != null)
        tmpfile.delete();

      // here is the reason save changes in node
      // node.save();
    } catch (IOException e) {
      // for debug purposes TEMPORARY
      e.printStackTrace();
    }
  }

  
  public void closeFile() throws Exception{
    remove();
  }

  public long seekFile(long pos, int typ) throws Exception{
    return 0;
  }
  
  public int readFile(byte[] buf, int len, int pos, long fileOff)
  throws Exception{
    return 0;
  }

  public void openFile(boolean createFlag) throws IOException {
    // TODO Auto-generated method stub
    
  }
  
}

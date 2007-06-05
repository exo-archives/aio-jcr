/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.cifs.smb.server;

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
 * Created by The eXo Platform SARL
 * Author : Karpenko
 */

public class JCRNetworkFile extends NetworkFile{
  private static final Log logger = ExoLogger.getLogger("org.exoplatform.services.CIFS.smb.server.JCRNetworkFile");
//  private static int id = 0; 
  private Node node;
  
  private FileChannel wrchannel;
  private File tmpfile;

  
  public JCRNetworkFile(Node n){
    super();
    node = n;
  }

  public JCRNetworkFile(){
    super();
    try{
    //this.createTemporaryFileChannel();
    }catch(Exception e){
      
    }
  }
  
  public void setNodeRef(Node n){
    node = n;
  }
  
  public Node getNodeRef(){
    return node;
  }
  
  private void createTemporaryFileChannel() throws Exception{
    //id++;
    
    String suf = String.valueOf(getFileId());
    tmpfile = new File("d:\\tf_"+suf+".adt");
    //this.tmpfile = f.createTempFile(t, f)
    wrchannel = new FileOutputStream(tmpfile,true).getChannel();
  }

  public int writeFile(byte[] buf, int dataPos, int dataLen, int offset) throws Exception {
    if (wrchannel == null) createTemporaryFileChannel();
    ByteBuffer byteBuffer = ByteBuffer.wrap(buf, dataPos, dataLen);
    int i =wrchannel.write(byteBuffer,offset);
    
    if(((long)(dataLen+offset)>=m_fileSize)&&(m_fileSize!=0)){
      FileInputStream fis = new FileInputStream(tmpfile);
      node.getNode("jcr:content").getProperty("jcr:data").setValue(fis);
      fis.close();
      wrchannel.close();
      tmpfile.delete();
      node.save();
      
      logger.debug("file data cmplitly writed into jcr node TEMPORARY");
    }
    
    return i;
  }
  
  public void flush() throws Exception{
    try{
      if(tmpfile!=null){
        FileInputStream st = new FileInputStream(tmpfile);
        
        if(node!= null){
        node.getNode("jcr:content").getProperty("jcr:data").setValue(st);
        node.save();
        }
        st.close();
      
        tmpfile.delete();
        tmpfile = null;
      }
    }catch(Exception e){
      e.printStackTrace();
    }

  }
  
  /**not used
   * 
   */
/*  public int readFile(byte[] buf, int maxCount, int dataPos, int offset) throws Exception {
    Node n = getNodeRef();
    InputStream is = n.getNode("jcr:content").getProperty("jcr:data").getStream();
    return is.read(buf,offset,maxCount);
  }*/
  
  public void truncFile(long len)throws IOException{
    try{
      if (wrchannel == null) createTemporaryFileChannel();
    
      wrchannel.truncate(len);
      m_fileSize = len;
    }catch(Exception e){
      throw new IOException(e.getMessage());
    }
  }
}

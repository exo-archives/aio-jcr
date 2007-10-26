/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SAS          All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.cifs.server.filesys;

import javax.jcr.Node;

/**
 * Created by The eXo Platform SAS Author : Sergey Karpenko
 * <sergey.karpenko@exoplatform.com.ua>
 * 
 * @version $Id: $
 */

public class Writer implements Runnable {

  private Node node;

  private  BufferedInputStream stream;
  
  Writer(Node n) {
    node = n;
  }

  public void uploadBuffer(byte[] buf) {
    stream.uploadBuffer(buf);
  }
  
  public void run() {
    try {
      stream = new BufferedInputStream();
      Thread th = new Thread(stream);
      th.setDaemon(true);
      th.start();

      node.getNode("jcr:content").getProperty("jcr:data").setValue(stream);
      
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
  
  

}

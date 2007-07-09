/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.jcr.load.blob.thread;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.exoplatform.services.jcr.impl.core.PropertyImpl;
import org.exoplatform.services.jcr.load.blob.TestConcurrent;

/**
 * Created by The eXo Platform SARL
 * Author : Peter Nedonosko
 *          peter.nedonosko@exoplatform.com.ua
 * 24.10.2006
 * @version $Id: ReadThread.java 12841 2007-02-16 08:58:38Z peterit $
 */
public class ReadThread extends UserThread {
  
  public ReadThread(Session threadSession) {
    super(threadSession);
  }
  
  public void testAction() {
    while (process) {
      readAction();
      try {
        sleep(1000);
      } catch(InterruptedException e) {
        threadLog.error("Sleep error: " + e.getMessage(), e);
      }
    }
  }
  
  public void readAction() {
    
    final List<String> readedNodes = new ArrayList<String>();
    int dataSizeInfo = 0;
    try {
      threadSession.refresh(false);
      Node testRoot = threadSession.getRootNode().getNode(TestConcurrent.TEST_ROOT);
      NodeIterator nodes = testRoot.getNodes();
      while (nodes.hasNext()) {
        Node node = nodes.nextNode();
        Node content = node.getNode("jcr:content");
        InputStream dataStream = null;
        int dataSize = 0;
        try {
          PropertyImpl data = (PropertyImpl) content.getProperty("jcr:data");
          dataStream = data.getStream();
          //threadLog.info("Read property " + data.getPath() + ", " + data.getInternalUUID());
          byte[] buff = new byte[1024 * 4];
          int read = 0;
          dataSize = 0;
          while ((read = dataStream.read(buff))>=0) {  
            dataSize += read;
          }
          if (dataSize != TestConcurrent.TEST_FILE_SIZE)
            threadLog.error("Wrong data size. " + dataSize + " but expected " + TestConcurrent.TEST_FILE_SIZE 
                + ". " + dataStream + ". " + data.getPath() + " " + data.getInternalIdentifier());
          else if (threadLog.isDebugEnabled())
            threadLog.debug("Read node: " + dataStream + ", " + node.getPath() + ", data: " + data.getInternalIdentifier());
        } catch(RepositoryException e) {
          threadLog.error("Repository error: " + e.getMessage() + ", " + dataSize + " bytes from " + TestConcurrent.TEST_FILE_SIZE, e);
        } catch(FileNotFoundException e) {
          threadLog.error("File not found, stream: " + dataStream + ", " + e.getMessage(), e);
        } finally {
          if (dataStream != null)
            dataStream.close();
          dataSizeInfo = dataSize;
        }
        readedNodes.add(node.getPath());
        //threadLog.info("Read node " + node.getPath());
      }
    } catch(Throwable th) {
      threadLog.error("Read error: " + th.getMessage() + ", " + dataSizeInfo + " bytes from " + TestConcurrent.TEST_FILE_SIZE, th);
    } finally {
      TestConcurrent.consumedNodes.addAll(readedNodes);
    }
  }
}

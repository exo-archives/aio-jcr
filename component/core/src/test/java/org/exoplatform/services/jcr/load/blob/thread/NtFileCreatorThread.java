/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SAS. All rights reserved.          *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.jcr.load.blob.thread;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;

import javax.jcr.Node;
import javax.jcr.Session;

import org.exoplatform.services.jcr.impl.core.PropertyImpl;
import org.exoplatform.services.jcr.load.blob.TestConcurrentItems;
import org.exoplatform.services.jcr.util.IdGenerator;

/**
 * Created by The eXo Platform SAS
 * Author : Peter Nedonosko
 *          peter.nedonosko@exoplatform.com.ua
 * 10.10.2007  
 *
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a>
 * @version $Id$
 */
public class NtFileCreatorThread extends UserThread {

  private final String testRootName;
  
  public NtFileCreatorThread(Session threadSession, String testRootName) {
    super(threadSession);
    this.testRootName = testRootName;
  }
  
  public void testAction() {
    while (process) {
      createAction();
      try {
        sleep(500);
      } catch(InterruptedException e) {
        threadLog.error("Sleep error: " + e.getMessage(), e);
      }
    }
  }
  
  public void createAction() {
    String nodeName = IdGenerator.generate();
    InputStream dataStream = null; 
    try {
      Node root = threadSession.getRootNode().getNode(TestConcurrentItems.TEST_ROOT);
      Node testRoot = root.getNode(testRootName);
      
      Node ntFile = testRoot.addNode(nodeName, "nt:file");
      Node contentNode = ntFile.addNode("jcr:content", "nt:resource");
      //dataStream =  new URL(TestSwap.URL_BIG_MEDIA_FILE).openStream();
      dataStream =  new FileInputStream(TestConcurrentItems.TEST_FILE);
      PropertyImpl data = (PropertyImpl) contentNode.setProperty("jcr:data", dataStream); 
      contentNode.setProperty("jcr:mimeType", "video/avi");
      contentNode.setProperty("jcr:lastModified", Calendar.getInstance());
      this.threadSession.save();
      if (threadLog.isDebugEnabled())
        threadLog.debug("Create node: " + ntFile.getPath() + ", data: " + data.getInternalIdentifier());
    } catch(Throwable th) {
      threadLog.error("Create error: " + th.getMessage(), th);
    } finally {
      if (dataStream != null)
        try {
          dataStream.close();
        } catch(IOException e) {
          threadLog.error("Stream read error: " + e.getMessage(), e);
        }
        try {
          this.threadSession.refresh(false);
        } catch(Throwable th) {
          threadLog.error("Session refresh error: " + th.getMessage());
        }
    }
  }

}
 
/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
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
import org.exoplatform.services.jcr.load.blob.TestConcurrent;
import org.exoplatform.services.jcr.util.IdGenerator;

/**
 * Created by The eXo Platform SARL
 * Author : Peter Nedonosko
 *          peter.nedonosko@exoplatform.com.ua
 * 24.10.2006
 * 
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a>
 * @version $Id: CreateThread.java 12841 2007-02-16 08:58:38Z peterit $
 */

public class CreateThread extends UserThread {

  
  public CreateThread(Session threadSession) {
    super(threadSession);
  }
  
  public void testAction() {
    while (process) {
      createAction();
      try {
        sleep(1500);
      } catch(InterruptedException e) {
        threadLog.error("Sleep error: " + e.getMessage(), e);
      }
    }
  }
  
  public void createAction() {
    
    String nodeName = IdGenerator.generate();
    InputStream dataStream = null; 
    try {
      Node testRoot = threadSession.getRootNode().getNode(TestConcurrent.TEST_ROOT);
      Node ntFile = testRoot.addNode(nodeName, "nt:file");
      Node contentNode = ntFile.addNode("jcr:content", "nt:resource");
      //dataStream =  new URL(TestSwap.URL_BIG_MEDIA_FILE).openStream();
      dataStream =  new FileInputStream(TestConcurrent.TEST_FILE);
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

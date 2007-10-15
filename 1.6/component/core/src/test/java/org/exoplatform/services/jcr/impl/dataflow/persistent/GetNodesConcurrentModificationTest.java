/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.jcr.impl.dataflow.persistent;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;

import org.exoplatform.commons.utils.MimeTypeResolver;
import org.exoplatform.services.jcr.JcrImplBaseTest;
import org.exoplatform.services.jcr.impl.core.NodeImpl;
import org.exoplatform.services.jcr.impl.core.SessionImpl;

/**
 * Created by The eXo Platform SARL
 *
 * 16.01.2007
 *
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a>
 * 
 */
public class GetNodesConcurrentModificationTest extends JcrImplBaseTest {

  public final String DATA_STRING = "DATA STRING";
  public final String DATA_PROPERTY = "data property";
  public final String FILE_DATA = "file data"; 
  public final String CHILDS_DATA = "child data";
  public final String LIBRARY = "library";
  
  protected NodeImpl testRoot;
  
  private List<String> fails = null; 
  private URL contentFile = null;  
    
  class NodesReader extends Thread {

    private final SessionImpl mySession;
    private boolean progress = true;
    
    NodesReader(String name, SessionImpl readerSession) {
      super(name);
      this.mySession = readerSession;
    }
    
    public void finish() {
      progress = false;
    }
    
    @Override
    public void run() {
      while (progress) {
        String nodePath = ""; 
        try {
          NodeImpl content = (NodeImpl) mySession.getItem("/concurrent_node/jcr:content");
                    
          // do test, request nodes
          NodeIterator contentIter = content.getNodes();
          while (progress && contentIter.hasNext()) {
            NodeImpl node = (NodeImpl) contentIter.nextNode();
            nodePath = node.getPath();
            
            if (node.getName().equals(FILE_DATA)) {
              String vs = node.getProperty(DATA_PROPERTY).getString();
              //log.info(getName() + " " + nodePath + " = " + vs);
              
            } else if (node.getName().equals(CHILDS_DATA)) {
              
              NodeIterator librIter = node.getNodes();
              while (progress && librIter.hasNext()) {
                
                NodeImpl libr = (NodeImpl) librIter.nextNode();
                NodeIterator filesIter = libr.getNodes();
                while (progress && filesIter.hasNext()) {
                  
                  NodeImpl file = (NodeImpl) filesIter.nextNode();
                  NodeImpl dataNode = (NodeImpl) file.getNode("jcr:content/fileData");
                  InputStream dataStream = dataNode.getProperty("jcr:data").getStream();
                  byte[] buff = new byte[4096];
                  try {
                    int res = 0;
                    int total = 0;
                    while ((res = dataStream.read(buff)) > 0) {
                      total =+ res; 
                    }
                    dataStream.close();
                    //log.info(getName() + " " + dataNode.getPath() + " " + total + " bytes");
                  } catch(IOException e) {
                    String msg = getName() + " >>> library jcr:content/fileData/jcr:data read error " + e + ". Last node " + nodePath;
                    log.error(msg, e);
                    addFail(msg);
                  }
                }
              }
            }
          }
        } catch(RepositoryException e) { 
          String msg = getName() + " >>> content node read error " + e + ". Last node " + nodePath;
          log.error(msg, e);
          addFail(msg);
        }
      }
    }
  }
  
  private synchronized void addFail(final String msg) {
    fails.add(msg);
  }
  
  @Override
  public void setUp() throws Exception {
    super.setUp();
    
    fails = new ArrayList<String>();
    contentFile = new URL("file:///" + new File("src/test/resources/index/test_index.doc").getAbsolutePath());
    initDB();
  }
  
  private void initDB() throws Exception  {
    if (!session.getRootNode().hasNode("concurrent_node")) {
      
      MimeTypeResolver mimeResolver = new MimeTypeResolver();
      
      long startTime = System.currentTimeMillis(); 
      int itemsCount = 0;
      
      testRoot = (NodeImpl) session.getRootNode().addNode("concurrent_node", "nt:file");
      
      NodeImpl content = (NodeImpl) testRoot.addNode("jcr:content", "nt:unstructured");
      NodeImpl data = (NodeImpl) content.addNode(FILE_DATA);
      data.setProperty(DATA_PROPERTY, DATA_STRING);
      itemsCount = itemsCount + 3;
      // add some SNSes
      for (int i=0; i<500; i++) {
        data = (NodeImpl) content.addNode(FILE_DATA);
        data.setProperty(DATA_PROPERTY, DATA_STRING + i);
        data.addNode("empty node"); 
        itemsCount = itemsCount + 3;
      }
      session.save();
      
      NodeImpl childData = (NodeImpl) content.addNode(CHILDS_DATA);
      
      for (int l = 1; l <= 4; l++) {
        Node subChild = childData.addNode(LIBRARY + " " + l);
        long startLibrary = System.currentTimeMillis();
        // add some nodes with diff names
        for (int i = 0; i < 500; i++) {
          long addTime = System.currentTimeMillis();
          String nodeName = "#" + i + " file";
          
          Node n = subChild.addNode(nodeName, "nt:file");
          Node nContent = n.addNode("jcr:content", "nt:unstructured");
          nContent.setProperty("currenTime", Calendar.getInstance());
          nContent.setProperty("info", "Info string");
          Node resource = nContent.addNode("fileData", "nt:resource");
          resource.setProperty("jcr:mimeType", mimeResolver.getMimeType("x.doc"));
          resource.setProperty("jcr:lastModified", Calendar.getInstance());
          resource.setProperty("jcr:data", contentFile.openStream());
          
          itemsCount = itemsCount + 8;
          
          log.info("add node " + nodeName + ", " + (System.currentTimeMillis() - addTime) + "ms, " 
              + (System.currentTimeMillis() - startTime) + "ms");
        }
        log.info(subChild.getPath() + " childs added " + (System.currentTimeMillis() - startLibrary) + "ms");
        startLibrary = System.currentTimeMillis();
        log.info(subChild.getPath() + " childs will be saved, wait few minutes...");
        session.save();
        log.info(subChild.getPath() + " childs saved " + (System.currentTimeMillis() - startLibrary) + "ms");
        
        itemsCount++;
      }
      
      data = (NodeImpl) content.addNode("description data");
      data.setProperty(DATA_PROPERTY, "Description record");
      itemsCount = itemsCount + 2;
      
      session.save();
      log.info("Items created " + itemsCount + ", " + (System.currentTimeMillis() - startTime) + "ms");
    }
  }

  @Override
  protected void tearDown() throws Exception {
    //testRoot.remove();
    //session.save();
    
    //super.tearDown();
  }

  public void testGetNodes() throws Exception {
    
    final long readersCount = 50; 
    
    List<NodesReader> readers = new ArrayList<NodesReader>();
    
    for (int i = 0; i<readersCount; i++) {
      NodesReader reader = new NodesReader("NR-" + i, 
          (SessionImpl) repository.login(session.getCredentials(), session.getWorkspace().getName()));
      readers.add(reader);
      reader.start();
    }
    
    try {
      Thread.sleep(readersCount * 10000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    
    for (NodesReader reader: readers) {
      reader.finish();
    }
    
    try {
      Thread.sleep(readersCount * 2000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    
    if (fails.size()>0) {
      String msgs = "";
      for (String msg: fails) {
        msgs = msgs + (msg + "\r");
      }
      fail("Test fails with messages\r " + msgs);
    }
    
  }
  
  
  
}

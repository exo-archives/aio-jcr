/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.jcr.benchmark.volumetry;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.ValueFactory;
import javax.jcr.nodetype.NodeType;

import org.apache.commons.logging.Log;
import org.exoplatform.jcr.benchmark.JCRTestBase;
import org.exoplatform.jcr.benchmark.JCRTestContext;
import org.exoplatform.services.jcr.impl.core.NodeImpl;
import org.exoplatform.services.jcr.impl.core.value.ValueFactoryImpl;
import org.exoplatform.services.log.ExoLogger;

import com.sun.japex.TestCase;

/**
 * Created by The eXo Platform SARL Author : Alex Reshetnyak
 * alex.reshetnyak@exoplatform.org.ua reshetnyak.alex@gmail.com 11.05.2007
 * 17:22:52
 * 
 * @version $Id: WriteNTFile.java 11.05.2007 17:22:52 rainfox
 */
public class WriteNTFile extends JCRTestBase {
  private Log      log         = ExoLogger.getLogger("benchmark.WriteNTFile");

  protected String testroot;

  protected String sMimeType;

  protected String sFile;

  protected String sTree;

  protected int    iteration;

  static int       nodeIndex;

  String           sNodeName   = "file";

  String           sFolderName = "folder";

  int              myIndex;

  Session          session;
  
  ValueFactory     valueFactory;

  Node             rootTestNode;

  Node             writeRoot;

  boolean          bSetFile;

  boolean          bReadDC;
  
  boolean          bReadproperty;
  
  boolean          bReadFile;
  
  boolean          bWriteLog;
  
  boolean          bConcurrent;

  @Override
  public void doPrepare(final TestCase tc, JCRTestContext context) {
    try {
      initParams(tc);

      session = context.getSession();
      
      valueFactory = session.getValueFactory();

      rootTestNode = addNodes(testroot, session.getRootNode());

      if (bConcurrent)
        myIndex = 0;
      else
        myIndex = getIndex();

      initTree();

      session = context.getSession();

    } catch (RepositoryException e) {
      log.error("ERROR: init WriteNTFile", e);
    } catch (IOException e) {
      log.error("ERROR", e);
    }
  }

  private void initParams(TestCase tc) {
    if (!tc.hasParam("test.testroot"))
      throw new RuntimeException("<test.testroot> parameter required");
    if (!tc.hasParam("test.mimetype"))
      throw new RuntimeException("<test.mimetype> parameter required");
    if (!tc.hasParam("test.file"))
      throw new RuntimeException("<test.file> parameter required");
    // if(!tc.hasParam("test.tree"))
    // throw new RuntimeException("<test.tree> parameter required");
    if (!tc.hasParam("test.iteration"))
      throw new RuntimeException("<test.iteration> parameter required");
    if (!tc.hasParam("test.readdc"))
      throw new RuntimeException("<test.readdc> parameter required");
    if (!tc.hasParam("test.readproperty"))
      throw new RuntimeException("<test.readproperty> parameter required");
    if (!tc.hasParam("test.readfile"))
      throw new RuntimeException("<test.readfile> parameter required");
    if (!tc.hasParam("test.writelog"))
      throw new RuntimeException("<test.writelog> parameter required");
    if (!tc.hasParam("test.concurrent"))
      throw new RuntimeException("<test.concurrent> parameter required");
    

    testroot = tc.getParam("test.testroot");
    sMimeType = tc.getParam("test.mimetype");
    sFile = tc.getParam("test.file");
    sTree = tc.getParam("test.tree");
    String sIteration = tc.getParam("test.iteration");
    iteration = Integer.valueOf(sIteration).intValue();

    bReadDC = Boolean.valueOf(tc.getParam("test.readdc")).booleanValue();
    bReadproperty = Boolean.valueOf(tc.getParam("test.readproperty")).booleanValue();
    bReadFile = Boolean.valueOf(tc.getParam("test.readfile")).booleanValue();
    bWriteLog = Boolean.valueOf(tc.getParam("test.writelog")).booleanValue();
    bConcurrent = Boolean.valueOf(tc.getParam("test.concurrent")).booleanValue();
    
    bSetFile = !sFile.equals("");
  }

  @Override
  public void doRun(TestCase tc, JCRTestContext context) {
    try {
      Node folder = rootTestNode.getNode(sFolderName + myIndex);
      NodeIterator ni = folder.getNodes();

      for (int i = 0; i < iteration; i++) {
        Node tempFile = ni.nextNode();
        if (bReadproperty)
          showProperty(tempFile);
          showProperty(tempFile.getNode("jcr:content"));
        if (bReadDC) 
          showDCProperty(tempFile);
      }

    } catch (RepositoryException e) {
      log.error("ERROR: reading", e);
    } catch (IOException e) {
      log.error("ERROR: reading", e);
    }
  }
  
  private Node addNodes(String sRoot, Node parentNode) throws RepositoryException {
    String mas[] = sRoot.split("/");

    Node temp = parentNode;

    for (int i = 1; i < mas.length; i++) {
      if (temp.hasNode(mas[i]))
        temp = temp.getNode(mas[i]);
      else {
        temp = temp.addNode(mas[i]);
        session.save();
      }
    }
    return temp;
  }

  private void initTree() throws RepositoryException, IOException {

    Node writeRoot = rootTestNode.addNode(sFolderName + myIndex, "nt:folder");

    for (int i = 0; i < iteration; i++) {

      Node nodeFile = writeRoot.addNode(sNodeName + myIndex + i, "nt:file");
      Node contentNode = nodeFile.addNode("jcr:content", "nt:resource");
      if (bSetFile)
        contentNode.setProperty("jcr:data", new FileInputStream(sFile));
      else
        contentNode.setProperty("jcr:data", new ByteArrayInputStream("".getBytes()));
      contentNode.setProperty("jcr:mimeType", sMimeType);
      contentNode.setProperty("jcr:lastModified", Calendar.getInstance());

      if (bReadDC) {
        nodeFile.addMixin("dc:elementSet");
        nodeFile.setProperty("dc:title", createMultiValue("0123456789"));
        nodeFile.setProperty("dc:subject", createMultiValue("0123456789"));
        nodeFile.setProperty("dc:description", createMultiValue("0123456789"));
        nodeFile.setProperty("dc:publisher", createMultiValue("0123456789"));
        nodeFile.setProperty("dc:date", createMultiValue(Calendar.getInstance()));
        nodeFile.setProperty("dc:resourceType", createMultiValue("0123456789"));
      }
    }

    session.save();
  }

  synchronized int getIndex() {
    return ++nodeIndex;
  }
  
  private Value[] createMultiValue(String sValue) {
    Value[] values = new Value[1];
    values[0] = valueFactory.createValue("0123456789");
    
    return values;
  } 
  
  private Value[] createMultiValue(Calendar date) {
    Value[] values = new Value[1];
    values[0] = valueFactory.createValue(date);
    
    return values;
  }

  public void showDCProperty(Node parent) throws RepositoryException {
    PropertyIterator pi = parent.getProperties();

    String sMix = parent.getPrimaryNodeType().getName();

    if (sMix.equals("nt:file")) {

      for (NodeType mt : parent.getMixinNodeTypes())
        sMix += " " + mt.getName();

      if (bWriteLog) 
        log.info(sMix + " " + parent.getPath());

      String[] dcprop = { "dc:title", "dc:subject", "dc:description", "dc:publisher", "dc:date", "dc:resourceType"};

      for (int i = 0; i < dcprop.length; i++) {
        Property propdc = parent.getProperty(dcprop[i]);

        String s = propdc.getValues()[0].getString();

        if (bWriteLog) 
          log.info("\t\t" + propdc.getName() + " " + PropertyType.nameFromValue(propdc.getType()) + " " + s);
      }
    }
  }
  
  public void showProperty(Node parent) throws RepositoryException, IOException {
    PropertyIterator pi = parent.getProperties();

    String sMix = parent.getPrimaryNodeType().getName();

    if (sMix.equals("nt:resource")) {

      for (NodeType mt : parent.getMixinNodeTypes()) {
        sMix += " " + mt.getName();
      }

      if (bWriteLog) 
        log.info(sMix + " " + parent.getPath());

      while (pi.hasNext()) {
        Property prop = pi.nextProperty();
        if (prop.getType() == PropertyType.BINARY) {
          
          int length = 0;
          if (bReadFile) 
            length = readStream(prop.getStream());
          
          if (bReadFile)
            if (bWriteLog)
              log.info("\t\t" + prop.getName() + " " + PropertyType.nameFromValue(prop.getType()) + " " + length + " byte");
          else
            if (bWriteLog)
              log.info("\t\t" + prop.getName() + " " + PropertyType.nameFromValue(prop.getType()));
          
        } else {
          String s = prop.getString();
          if (s.length() > 64)
            s = s.substring(0, 64);
          if (bWriteLog)
            log.info("\t\t" + prop.getName() + " " + PropertyType.nameFromValue(prop.getType()) + " " + s);
        }
      }
    }
  }
  
  public int readStream(InputStream is) throws IOException{
    int length = 0;
    int len;
    byte buf[] = new  byte[4049];
    while ((len = is.read(buf)) > 0)
      length += len;
    return length;
  }
  
}

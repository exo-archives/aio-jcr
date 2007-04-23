/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.applications.repoload;

import java.util.HashMap;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.nodetype.NodeType;

import org.apache.commons.logging.Log;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SARL Author : Alex Reshetnyak
 * alex.reshetnyak@exoplatform.org.ua reshetnyak.alex@gmail.com 16.04.2007
 * 12:52:43
 * 
 * @version $Id: DataReaderTh.java 16.04.2007 12:52:43 rainfox
 */
public class DataReaderTh implements Runnable {

  private Log                       log;

  protected HashMap<String, String> mapConfig;

  private int                       ntFolderCount;

  private int                       ntFileCount;

  private String                    threadName;

  private Node                      startNode;

  private Thread                    thReader;

  private int                       iteration;

  private long                      end, start, time = 0;

  private boolean                   readProperty;

  public DataReaderTh(Node startNode, String threadName, HashMap<String, String> conf) {
    log = ExoLogger.getLogger("repload.DataReaderTh_" + threadName);
    mapConfig = conf;
    this.threadName = threadName;
    this.startNode = startNode;
    thReader = new Thread(this, threadName);
    this.iteration = Integer.valueOf(mapConfig.get("-iteration")).intValue();
    this.readProperty = Boolean.valueOf(mapConfig.get("-readprop")).booleanValue();
    log.info("-readprop = " + readProperty);
  }

  public void readChilds(Node parent) throws RepositoryException {

    String primaryType = parent.getPrimaryNodeType().getName();

    if (primaryType.equals("nt:folder")) {
      ntFolderCount++;
      log.info("\t" + ntFolderCount + " nt:folder has been raed");
        NodeIterator ni = parent.getNodes();
        if (ni.hasNext()) {
          while (ni.hasNext()) {
            Node n1 = ni.nextNode();
            readChilds(n1);
          }
        }
    } else if (primaryType.equals("nt:file")) {
      ntFileCount++;
      log.info("\t" + ntFileCount + " nt:file has been raed");
      if (readProperty) {
        showDCProperty(parent);
        
        NodeIterator ni = parent.getNodes();
        if (ni.hasNext()) {
          while (ni.hasNext()) {
            Node n1 = ni.nextNode();
            readChilds(n1);
          }
        } else {
          showProperty(parent);
        }
      }
    } else if (readProperty)
      showProperty(parent);
  }

  public void showDCProperty(Node parent) throws RepositoryException {
    PropertyIterator pi = parent.getProperties();

    String sMix = parent.getPrimaryNodeType().getName();

    if (sMix.equals("nt:file")) {

      for (NodeType mt : parent.getMixinNodeTypes())
        sMix += " " + mt.getName();

      log.info(sMix + " " + parent.getPath());

      if (mapConfig.get("-readdc").equals("true")) {
        String[] dcprop = { "dc:title", "dc:creator", "dc:subject", "dc:description",
            "dc:publisher" };

        for (int i = 0; i < dcprop.length; i++) {
          Property propdc = parent.getProperty(dcprop[i]);

          String s = propdc.getValues()[0].getString();

          log.info("\t\t" + propdc.getName() + " " + PropertyType.nameFromValue(propdc.getType())
              + " " + s);
        }
      }
    }

  }

  public void showProperty(Node parent) throws RepositoryException {
    PropertyIterator pi = parent.getProperties();

    String sMix = parent.getPrimaryNodeType().getName();

    if (sMix.equals("nt:resource")) {

      for (NodeType mt : parent.getMixinNodeTypes()) {
        sMix += " " + mt.getName();
      }

      log.info(sMix + " " + parent.getPath());

      while (pi.hasNext()) {
        Property prop = pi.nextProperty();
        if (prop.getType() == PropertyType.BINARY) {
          log.info("\t\t" + prop.getName() + " " + PropertyType.nameFromValue(prop.getType()));
        } else {
          String s = prop.getString();
          if (s.length() > 64)
            s = s.substring(0, 64);
          log.info("\t\t" + prop.getName() + " " + PropertyType.nameFromValue(prop.getType()) + " "
              + s);
        }
      }
    }
  }

  public void run() {
    try {
      start = System.currentTimeMillis();

      for (int i = 0; i < iteration; i++)
        readChilds(startNode);

      end = System.currentTimeMillis();

      time = end - start;

    } catch (RepositoryException e) {
      log.error("Error: read data", e);
      time = -1;
    }
  }

  public void startRead() {
    thReader.start();
  }

  public long getTimeAdding() {
    return time;
  }

  public int getNTCount() {
    return ntFileCount;
  }

  public String getThreadName() {
    return threadName;
  }
}

/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.jcr.impl.xml;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;

import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;

//import org.apache.ws.commons.util.Base64$SAXEncoder;
import org.apache.ws.commons.util.Base64;
import org.apache.ws.commons.util.Base64.SAXEncoder;
import org.exoplatform.services.jcr.dataflow.ItemDataConsumer;
import org.exoplatform.services.jcr.dataflow.ItemDataTraversingVisitor;
import org.exoplatform.services.jcr.datamodel.NodeData;
import org.exoplatform.services.jcr.datamodel.PropertyData;
import org.exoplatform.services.jcr.datamodel.ValueData;
import org.exoplatform.services.jcr.impl.Constants;
import org.exoplatform.services.jcr.impl.core.LocationFactory;
import org.exoplatform.services.jcr.impl.core.SessionImpl;
import org.exoplatform.services.jcr.impl.core.value.BinaryValue;
import org.exoplatform.services.jcr.impl.dataflow.NodeDataOrderComparator;
import org.exoplatform.services.jcr.impl.dataflow.PropertyDataOrderComparator;
import org.exoplatform.services.jcr.impl.dataflow.TransientValueData;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/**
 * @author <a href="mailto:Sergey.Kabashnyuk@gmail.com">Sergey Kabashnyuk</a>
 * @version
 */
public abstract class ExportXmlVisitor extends ItemDataTraversingVisitor {

  /**
   * Actual value of each <code>PropertyType.BINARY</code> property is
   * recorded using Base64 encoding
   */
  public static final int        BINARY_PROCESS = 0;

  /**
   * Any properties of <code>PropertyType.BINARY</code> will be ignored and
   * will not appear in the serialized output
   */
  public static final int        BINARY_SKIP    = 1;

  /**
   * Serialized only definition of of each <code>PropertyType.BINARY</code>
   * property without values
   */
  public static final int        BINARY_EMPTY   = 2;

  private int                    binaryConduct  = BINARY_PROCESS;

  protected boolean              noRecurse;

  protected final ContentHandler contentHandler;

  protected final SessionImpl    session;

  protected LocationFactory      locationFactory;

  public ExportXmlVisitor(ContentHandler handler,
      SessionImpl session,
      ItemDataConsumer dataManager,
      boolean skipBinary,
      boolean noRecurse) {

    super(dataManager, noRecurse ? 1 : -1);
    this.session = session;
    if (session instanceof SessionImpl) {
      this.locationFactory = ((SessionImpl) session).getLocationFactory();
    } else {
      throw new java.lang.IllegalArgumentException("session not an instanceof SessionImpl");
    }

    this.contentHandler = handler;
    setBinaryConduct(skipBinary ? BINARY_SKIP : BINARY_PROCESS);
    setNoRecurse(noRecurse);
  }

  /**
   * Specify how properties of <code>PropertyType.BINARY</code> serialized
   * 
   * @see <code>BINARY_PROCESS</code>,<code>BINARY_SKIP</code>,
   *      <code>BINARY_EMPTY</code>
   */
  public int getBinaryConduct() {
    return binaryConduct;
  }

  public void setBinaryConduct(int binaryConduct) {
    if (binaryConduct != BINARY_PROCESS && binaryConduct != BINARY_SKIP
        && binaryConduct != BINARY_EMPTY)
      throw new java.lang.IllegalArgumentException("binaryConduct must be one of "
          + "BINARY_PROCESS,BINARY_SKIP, BINARY_EMPTY");
    this.binaryConduct = binaryConduct;
  }

  public boolean isNoRecurse() {
    return noRecurse;
  }

  public void setNoRecurse(boolean noRecurse) {
    this.noRecurse = noRecurse;
  }

  public void export(NodeData node) throws RepositoryException, SAXException {
    if (contentHandler != null) {
      contentHandler.startDocument();
      startPrefixMapping();
      node.accept(this);
      endPrefixMapping();
      contentHandler.endDocument();
    }
  }

  protected void writeValueData(ValueData data, int type) throws Exception {
    char[] charValue = "".toCharArray();
    switch (type) {
    case PropertyType.BINARY:
      if (getBinaryConduct() == BINARY_SKIP)
        break;
      else if (getBinaryConduct() == BINARY_EMPTY) {
        try {
          charValue = new BinaryValue("").getString().toCharArray();
        } catch (IOException e) {
          throw new RepositoryException(e);
        }
      } else {

        InputStream is = data.getAsStream();
      
        Base64.SAXEncoder encoder = new Base64.SAXEncoder(new char[4096], 0, null, contentHandler);
        Base64.EncoderOutputStream eos = new Base64.EncoderOutputStream(encoder);

        byte[] buffer = new byte[4096];
        int len;
        while ((len = is.read(buffer)) > 0) {
          
          if (len < 4096) {
            byte[] buffer2 = new byte[len];
            System.arraycopy(buffer, 0, buffer2, 0, len);
            buffer = buffer2;
          }
          eos.write(buffer);

        }
        is.close();
        eos.close();
        buffer = null;
      }

      break;

    case PropertyType.NAME:
    case PropertyType.DATE:
    case PropertyType.PATH:
      charValue = session.getValueFactory().loadValue((TransientValueData) data, type).getString()
          .toCharArray();
      contentHandler.characters(charValue, 0, charValue.length);
      break;
    default:
      charValue = new String(data.getAsByteArray()).toCharArray();
      contentHandler.characters(charValue, 0, charValue.length);
      break;
    }
  }

  protected void startPrefixMapping() throws RepositoryException, SAXException {
    String[] prefixes = session.getNamespacePrefixes();
    for (String prefix : prefixes) {
      // skeep xml prefix
      if (prefix.equals(Constants.NS_XML_PREFIX))
        continue;
      contentHandler.startPrefixMapping(prefix, session.getNamespaceURI(prefix));
    }

  };

  protected void endPrefixMapping() throws RepositoryException, SAXException {
    String[] prefixes = session.getNamespacePrefixes();
    for (String prefix : prefixes) {
      contentHandler.endPrefixMapping(prefix);
    }
  };

  @Override
  public void visit(NodeData node) throws RepositoryException {
    try {
      entering(node, currentLevel);
      if (maxLevel == -1 || currentLevel < maxLevel) {
        currentLevel++;

        List<PropertyData> properies = dataManager.getChildPropertiesData(node);
        // Sorting properties
        Collections.sort(properies, new PropertyDataOrderComparator());
        for (PropertyData data : properies)
          data.accept(this);

        if (!isNoRecurse() && currentLevel > 0) {
          List<NodeData> nodes = dataManager.getChildNodesData(node);
          // Sorting nodes
          Collections.sort(nodes, new NodeDataOrderComparator());
          for (NodeData data : nodes)
            data.accept(this);
        }
        currentLevel--;
      }
      leaving(node, currentLevel);
    } catch (RepositoryException re) {
      currentLevel = 0;
      throw re;
    }

  }

  private void writeAttribute(String qname, String value) {
    // buffer.append(" " + qname + "=\"" + value + "\"");
  }

  private void writeNamespaces() throws RepositoryException {
    String[] keys = session.getAllNamespacePrefixes();
    for (int i = 0; i < keys.length; i++)
      if (keys[i].length() > 0)
        writeAttribute("xmlns:" + keys[i], session.getNamespaceURIByPrefix(keys[i]));
  }

}

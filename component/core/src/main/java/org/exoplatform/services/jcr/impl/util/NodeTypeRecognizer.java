/***************************************************************************
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.jcr.impl.util;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * Created by The eXo Platform SARL .
 * 
 * @author <a href="mailto:geaz@users.sourceforge.net">Gennady Azarenkov</a>
 * @version $Id: NodeTypeRecognizer.java 12841 2007-02-16 08:58:38Z peterit $
 */

public class NodeTypeRecognizer {

  public static final int SYS = 1;

  public static final int DOC = 2;

  public static int recognize(InputStream is) throws IOException, SAXException,
      ParserConfigurationException {

    DocumentBuilderFactory dfactory = DocumentBuilderFactory.newInstance();
    dfactory.setNamespaceAware(true);
    DocumentBuilder docBuilder = dfactory.newDocumentBuilder();
    Document doc = docBuilder.parse(is);

    String namespaceURI = doc.getNamespaceURI();
    String name = doc.getDocumentElement().getNodeName();
    return recognize(namespaceURI, name);
  }

  public static int recognize(String namespaceURI, String qName) {

    boolean hasSysName = qName != null && qName.toUpperCase().toLowerCase().startsWith("sv:");
    if ("http://www.jcp.org/jcr/sv/1.0".equals(namespaceURI) && hasSysName) {
      return SYS;
    } else {
      return DOC;
    }
  }

}
